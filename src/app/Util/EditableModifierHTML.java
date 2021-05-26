package app.Util;

public class EditableModifierHTML
{
    public static String setEditable(String htmlCode)
    {
        String modifiedHTML = htmlCode.replaceAll(
            "contenteditable=\"false\"", "contenteditable=\"true\""
        );

        return modifiedHTML;
    }

    public static String setNonEditable(String htmlCode)
    {
        String modifiedHTML = htmlCode.replaceAll(
            "contenteditable=\"true\"", "contenteditable=\"false\""
        );

        return modifiedHTML;
    }
}
