
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public class Directory implements IDirectory {
        
    private File directory;
    private String path;
    
    public Directory(String path) {
        this.path = path;
        directory = new File(Defaults.RootDirectoryPath + "/" + path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    public static String absolutePathForNote(Note note) {
        return absolutePathForNote(note.getPath(), note.getName());
    }
    
    public static String absolutePathForNote(String path, String name) {
        return Defaults.RootDirectoryPath + "/" + path + "/" + name + "." + Defaults.NoteFileExtension;
    }
    
    @Override
    public List<String> getDirectoriesNames() {
        List<String> directoriesNames = Arrays.asList(directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        }));
        return directoriesNames;
    }
    
    @Override
    public List<String> getNotesNames() {
        if (!directory.exists()) {
            return new ArrayList<>();
        }
        List<String> notesFilesNames = Arrays.asList(directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                if (name.endsWith("." + Defaults.NoteFileExtension)) {
                    return new File(current, name).isFile();
                }
                return false;
            }
        }));
        ArrayList<String> notesNames = new ArrayList<>();
        for (String name : notesFilesNames) {
            notesNames.add(name.substring(0, name.lastIndexOf(".")));

        }
        return notesNames;
    }
    
    @Override
    public List<IDirectory> getDirectories() {
        List<String> directoriesNames = getDirectoriesNames();
        List<IDirectory> directories = new ArrayList<>();
        for (String name : directoriesNames) {
            directories.add(new Directory(path + "/" + name));
        }
        return directories;
    }
    
    @Override
    public List<INote> getNotes(boolean loadContent) {
        List<String> notesNames = getNotesNames();
        List<INote> notes = new ArrayList<>();
        for (String name : notesNames) {
            Note note = Note.load(absolutePathForNote(path, name), loadContent);
            if (note == null) {
                continue;
            }
            notes.add(note);
        }
        return notes;
    }
    
    @Override
    public INote createNote() {
        String name = generateNoteName();
        Note note = new Note(path, name);
        note.save();
        return note;
    }
    
    @Override
    public IDirectory createDirectory() {
        return createDirectory(Defaults.NewDirectoryName);
    }
    
    @Override
    public IDirectory createDirectory(String name) {
        Directory newDirectory = new Directory(path + "/" + getUniqueName(name));
        return newDirectory;
    }
    
    public boolean setPath(String path) {
        File newFile = new File(Defaults.RootDirectoryPath + "/" + path);
        if (newFile.isDirectory()) {
            this.path = path;
            directory = newFile;
            return true;
        }
        return false;
    }
    
    @Override
    public String getPath() {
        String pathToReturn = this.path;
        pathToReturn = "/" + pathToReturn + "/";
        while (pathToReturn.contains("//")) {
            pathToReturn = pathToReturn.replace("//", "/");
        }
        return pathToReturn;
    }
    
    @Override
    public String getAbsolutePath() {
        return Defaults.RootDirectoryPath + getPath();
    }
    
    @Override
    public String getName() {
        String name = path.substring(path.lastIndexOf("/"), path.length()).replace("/", "");
        return name.isEmpty() ? "Notes" : name;
    }
    
    @Override
    public boolean setName(String name) {
        if (this.getName().equals(name)) {
            return false;
        }
        String parentPath = path.substring(0, path.lastIndexOf("/"));
        return move(new Directory(parentPath), name);
    }
    
    @Override
    public boolean move(IDirectory desination) {
        return move(desination, getName());
    }
    
    @Override
    public boolean move(IDirectory destination, String name) {
        
        String parentPath = path.substring(0, path.lastIndexOf("/"));
        Directory parentDirectory = new Directory(parentPath);
        String parentDirectoryPath = parentDirectory.getPath();
        String destinationDirectoryPath = destination.getPath();
        if (destinationDirectoryPath.equals(parentDirectoryPath) &&
                getName().equals(name)) {
            return false;
        }
        
        try {
            String newPath = destination.getPath() + "/" + destination.getUniqueName(name);
            Path sourcePath = Paths.get(Defaults.RootDirectoryPath + "/" + path);
            Path destinationPath = Paths.get(Defaults.RootDirectoryPath + "/" + newPath);
            Files.move(sourcePath, destinationPath, REPLACE_EXISTING);
            path = newPath;
            directory = new File(destinationPath.toString());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    @Override
    public boolean remove() {
        return deleteDirectory(directory);
    }

    @Override
    public boolean removeNote(INote note) {
        return (new File(absolutePathForNote(path, note.getName()))).delete();
    }
    
    @Override
    public boolean moveNote(INote note, IDirectory destination) {
        if (getPath().equals(destination.getPath())) {
            return false;
        }
        
        try {
            Path sourcePath = Paths.get(absolutePathForNote((Note) note));
            Path destinationPath = Paths.get(Defaults.RootDirectoryPath + "/" + 
                    destination.getPath() + "/" + 
                    destination.generateNoteName() + "." + Defaults.NoteFileExtension);
            Files.move(sourcePath, destinationPath, REPLACE_EXISTING);
            note.reload(destinationPath.toString(), false);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    @Override
    public String generateNoteName() {
        String name;
        List<String> notesNames = getNotesNames();
        do {
            name = String.valueOf(System.currentTimeMillis());
        } while (notesNames.contains(name));
        return name;
    }
    
    @Override
    public String getUniqueName(String name) {
        int counter = 0;
        List<String> directoriesNames = getDirectoriesNames();
        while (directoriesNames.contains(name + (counter == 0 ? "" : " " + counter))) {
            counter++;
        }
        return name + (counter == 0 ? "" : " " + counter);
    }
    
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null){
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
