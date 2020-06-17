package com.zacademy.notekeeperv1;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {

    /**
     * to ensure that each test runs in a consistent state. in this case make sure that the list
     * of notes always starts in a consistent state
     */
    @Before
    public void setUp() {
        DataManager dm = DataManager.getInstance();
        dm.getNotes().clear();
        dm.initializeExampleNotes();// each test starts with exactly the same set of notes, whether
        //it runs by itself or together with all others in its class

    }

    @Test
    public void createNewNote() {
        DataManager dm = DataManager.getInstance();

        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of my test note";

        int noteIndex = dm.createNewNote();
        NoteInfo newNote = dm.getNotes().get(noteIndex);

        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        NoteInfo compareNote = dm.getNotes().get(noteIndex);
// we expect to get back the first parameter, the second parameter is what we actually get back.
        assertEquals(course, compareNote.getCourse());
        assertEquals(noteTitle, compareNote.getTitle());
        assertEquals(noteText, compareNote.getText());

    }

    /**
     * We are testing if if our notes have same course, same titles, but different bodies; if findNotes()
     * method returns the correct results. Therefore just testing the edge case to make sure we find
     * the correct note when two notes are similar.
     */

    @Test
    public void findSimilarNotes() {

        DataManager dm = DataManager.getInstance();

        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText1 = "This is the body of my test note";
        final String noteText2 = "This is the body of my second test note";

        int noteIndex1 = dm.createNewNote();
        NoteInfo newNote1 = dm.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(noteTitle);
        newNote1.setText(noteText1);

        int noteIndex2 = dm.createNewNote();
        NoteInfo newNote2 = dm.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(noteTitle);
        newNote2.setText(noteText2);

        int foundIndex1 = dm.findNote(newNote1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = dm.findNote(newNote2);
        assertEquals(noteIndex2, foundIndex2);


    }
}