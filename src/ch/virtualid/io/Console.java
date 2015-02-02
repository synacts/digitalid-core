package ch.virtualid.io;

import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.io.EscapeOptionException;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class helps to read from standard input and write to standard output.
 * 
 * @see Option
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Console {
    
    /**
     * Stores the buffered reader from the standard input.
     */
    private static final @Nonnull BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    /**
     * Writes the given string to the standard output (including a new line).
     * 
     * @param string the string to be written to the standard output.
     */
    public static void write(@Nonnull String string) {
        System.out.println(string);
    }
    
    /**
     * Writes a new line to the standard output.
     */
    public static void write() {
        System.out.println();
    }
    
    /**
     * Flushes the standard output.
     */
    public static void flush() {
        System.out.flush();
    }
    
    /**
     * Reads a line from the standard input.
     * 
     * @return the line read from the standard input.
     */
    private static @Nonnull String read() {
        try {
            final @Nullable String input = reader.readLine();
            if (input == null) throw new IOException("The end of the standard input stream has been reached.");
            return input;
        } catch (@Nonnull IOException exception) {
            throw new ShouldNeverHappenError("Could not read from the standard input.", exception);
        }
    }
    
    /**
     * Reads a string from the standard input.
     * 
     * @return the string read from the standard input.
     */
    public static @Nonnull String readString() {
        return read();
    }
    
    /**
     * Reads an integer from the standard input.
     * 
     * @return the integer read from the standard input.
     */
    public static @Nonnull BigInteger readInteger() {
        while (true) {
            final @Nonnull String input = read();
            try {
                return new BigInteger(input);
            } catch (@Nonnull NumberFormatException exception) {
                System.out.print("Could not parse the input. Please enter an integer: ");
            }
        }
    }
    
    /**
     * Reads a 'long' from the standard input.
     * 
     * @return the 'long' read from the standard input.
     */
    public static long readLong() {
        while (true) {
            final @Nonnull String input = read();
            try {
                return Long.decode(input);
            } catch (@Nonnull NumberFormatException exception) {
                System.out.print("Could not parse the input. Please enter a 'long': ");
            }
        }
    }
    
    /**
     * Reads an 'int' from the standard input.
     * 
     * @return the 'int' read from the standard input.
     */
    public static int readInt() {
        while (true) {
            final @Nonnull String input = read();
            try {
                return Integer.decode(input);
            } catch (@Nonnull NumberFormatException exception) {
                System.out.print("Could not parse the input. Please enter an 'int': ");
            }
        }
    }
    
    /**
     * Reads a 'short' from the standard input.
     * 
     * @return the 'short' read from the standard input.
     */
    public static short readShort() {
        while (true) {
            final @Nonnull String input = read();
            try {
                return Short.decode(input);
            } catch (@Nonnull NumberFormatException exception) {
                System.out.print("Could not parse the input. Please enter a 'short': ");
            }
        }
    }
    
    /**
     * Reads a 'byte' from the standard input.
     * 
     * @return the 'byte' read from the standard input.
     */
    public static byte readByte() {
        while (true) {
            final @Nonnull String input = read();
            try {
                return Byte.decode(input);
            } catch (@Nonnull NumberFormatException exception) {
                System.out.print("Could not parse the input. Please enter a 'byte': ");
            }
        }
    }
    
    /**
     * Reads a 'boolean' from the standard input.
     * 
     * @return the 'boolean' read from the standard input.
     */
    public static boolean readBoolean() {
        while (true) {
            final @Nonnull String input = read();
            if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y")) return true;
            if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n")) return false;
            System.out.print("Could not parse the input. Please enter 'yes' or 'no': ");
        }
    }
    
    /**
     * Writes the given string to the standard output and reads a string from the standard input.
     * 
     * @param string the string to write to the standard output.
     * @return the string read from the standard input.
     */
    public static @Nonnull String readString(String string) {
        System.out.print(string);
        return readString();
    }
    
    /**
     * Writes the given string to the standard output and reads an integer from the standard input.
     * 
     * @param string the string to write to the standard output.
     * @return the integer read from the standard input.
     */
    public static @Nonnull BigInteger readInteger(String string) {
        System.out.print(string);
        return readInteger();
    }
    
    /**
     * Writes the given string to the standard output and reads an 'int' from the standard input.
     * 
     * @param string the string to write to the standard output.
     * @return the 'int' read from the standard input.
     */
    public static int readInt(String string) {
        System.out.print(string);
        return readInt();
    }
    
    /**
     * Writes the given string to the standard output and reads a 'short' from the standard input.
     * 
     * @param string the string to write to the standard output.
     * @return the 'short' read from the standard input.
     */
    public static short readShort(String string) {
        System.out.print(string);
        return readShort();
    }
    
    /**
     * Writes the given string to the standard output and reads a 'byte' from the standard input.
     * 
     * @param string the string to write to the standard output.
     * @return the 'byte' read from the standard input.
     */
    public static byte readByte(String string) {
        System.out.print(string);
        return readByte();
    }
    
    /**
     * Writes the given string to the standard output and reads a 'boolean' from the standard input.
     * 
     * @param string the string to write to the standard output.
     * @return the 'boolean' read from the standard input.
     */
    public static boolean readBoolean(String string) {
        System.out.print(string);
        return readBoolean();
    }
    
    
    /**
     * Stores the available options for the user.
     */
    private static final @Nonnull FreezableList<Option> options = new FreezableLinkedList<Option>();
    
    /**
     * Adds the given option to the list of options.
     * 
     * @param option the option to add.
     */
    public static void addOption(@Nonnull Option option) {
        options.add(option);
    }
    
    /**
     * Starts an infinite loop that lets the user choose from the list of options.
     */
    public static void start() {
        while (true) {
            write("---------------------------------------------------------");
            write();
            write("You have the following options:");
            final int size = options.size();
            for (int i = 0; i < size; i++) {
                write("- " + (size >= 10 && i < 10 ? " " : "") + i + ": " + options.get(i).getDescription());
            }
            write();
            final int input = readInt("Execute the option: ");
            write();
            if (input >= 0 && input < size) {
                try { options.get(input).execute(); } catch (@Nonnull EscapeOptionException exception) {}
            } else {
                write("Please choose one of the given options!");
            }
            write();
        }
    }
    
}
