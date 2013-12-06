package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextComponent extends BaseComponent
{

    private static final Pattern url = Pattern.compile( "^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$" );

    public static BaseComponent[] fromLegacyText(String message)
    {
        ArrayList<BaseComponent> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher( message );

        for (int i = 0; i < message.length(); i++)
        {
            char c = message.charAt( i );
            if (c == ChatColor.COLOR_CHAR)
            {
                i++;
                c = message.charAt( i );
                if (c >= 'A' && c <= 'Z')
                {
                    c += 32;
                }
                if (builder.length() > 0)
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    components.add( old );
                }
                ChatColor format = ChatColor.getByChar( c );
                switch (format)
                {
                    case BOLD:
                        component.setBold( true );
                        break;
                    case ITALIC:
                        component.setItalic( true );
                        break;
                    case UNDERLINE:
                        component.setUnderlined( true );
                        break;
                    case STRIKETHROUGH:
                        component.setStrikethrough( true );
                        break;
                    case MAGIC:
                        component.setObfuscated( true );
                        break;
                    case RESET:
                        format = ChatColor.WHITE;
                    default:
                        component = new TextComponent();
                        component.setColor( format );
                        break;
                }
                continue;
            }
            int pos = message.indexOf( ' ', i );
            if (pos == -1) pos = message.length();
            if (matcher.region( i, pos ).find())
            { //Web link handling

                if (builder.length() > 0)
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    components.add( old );
                }

                TextComponent old = component;
                component = new TextComponent( old );
                ClickEvent clickEvent = new ClickEvent();
                clickEvent.setAction( ClickEvent.Action.OPEN_URL );
                String urlString = message.substring( i, pos );
                if (urlString.startsWith( "http" ))
                {
                    component.setText( urlString );
                    clickEvent.setValue( urlString );
                } else
                {
                    component.setText( urlString );
                    clickEvent.setValue( "http://" + urlString );
                }
                component.setClickEvent( clickEvent );
                components.add( component );
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append( c );
        }
        if (builder.length() > 0)
        {
            component.setText( builder.toString() );
            components.add( component );
        }
        return components.toArray( new BaseComponent[components.size()] );
    }

    private String text;

    public TextComponent(TextComponent old)
    {
        super( old );
        setText( old.getText() );
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        builder.append( text );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        builder.append( getColor() );
        if (isBold()) builder.append( ChatColor.BOLD );
        if (isItalic()) builder.append( ChatColor.ITALIC );
        if (isUnderlined()) builder.append( ChatColor.UNDERLINE );
        if (isStrikethrough()) builder.append( ChatColor.STRIKETHROUGH );
        if (isObfuscated()) builder.append( ChatColor.MAGIC );
        builder.append( text );
        super.toLegacyText( builder );
    }

    @Override
    public String toString()
    {
        return String.format( "TextComponent{text=%s, %s}", text, super.toString() );
    }
}
