package app.model;

/**
 * Enum class indicating if the current note set is one newly created, loaded an
 * existing file, edited (new note or deletion of notes), or saved (to current
 * file or new file).
 */
public enum NoteSetStatus
{
    NEW,
    LOADED,
    CHANGED,
    SAVED
}
