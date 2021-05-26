package app.model;

import java.io.Serializable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Model class that represents an individual note. Contains the full note in
 * HTML format, the date and time when this note was created, and a parsed
 * text-only preview of the note's content.
 */
public class Note implements Serializable
{
    private String dateTimeOfNote;
    private String fullNote;
    private String parsedPreview;

    public Note()
    {
        dateTimeOfNote = null;
        fullNote = null;
        parsedPreview = null;
    }

    public Note(String dateTimeOfNote, String fullNote)
    {
        this.dateTimeOfNote = dateTimeOfNote;
        this.fullNote = fullNote;
        this.parsedPreview = parseText();
        if (parsedPreview.length() > 120)
        {
            parsedPreview = parsedPreview.substring(0, 121);
            parsedPreview += "...";
        }
    }

    public String getDateTimeOfNote()
    {
        return this.dateTimeOfNote;
    }

    public void setDateTimeOfNote(String dateTimeOfNote)
    {
        this.dateTimeOfNote = dateTimeOfNote;
    }

    public String getFullNote()
    {
        return this.fullNote;
    }

    public void setFullNote(String fullNote)
    {
        this.fullNote = fullNote;
    }

    public String getParsedPreview()
    {
        return this.parsedPreview;
    }

    public void setParsedPreview(String parsedPreview)
    {
        this.parsedPreview = parsedPreview;
    }

    private String parseText()
    {
        Document document = Jsoup.parse(fullNote);
        return document.text();
    }
}
