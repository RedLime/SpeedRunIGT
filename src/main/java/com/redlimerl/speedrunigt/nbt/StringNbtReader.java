package com.redlimerl.speedrunigt.nbt;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public class StringNbtReader {
    private static final Logger LOGGER = Logger.getLogger("String NBT Reader");

    public static NbtElement method_7377(String string) {
        string = string.trim();
        int var1 = getTopElementCount(string);
        if (var1 != 1) {
            throw new NbtException("Encountered multiple top tags, only one expected");
        } else {
            NbtParser var2;
            if (string.startsWith("{")) {
                var2 = method_7379("tag", string);
            } else {
                var2 = method_7379(getKey(string, false), getValue(string, false));
            }

            return var2.parse();
        }
    }

    static int getTopElementCount(String string) {
        int var1 = 0;
        boolean var2 = false;
        Stack<Character> var3 = new Stack<>();

        for (int var4 = 0; var4 < string.length(); var4++) {
            char var5 = string.charAt(var4);
            if (var5 == '"') {
                if (var4 > 0 && string.charAt(var4 - 1) == '\\') {
                    if (!var2) {
                        throw new NbtException("Illegal use of \\\": " + string);
                    }
                } else {
                    var2 = !var2;
                }
            } else if (!var2) {
                if (var5 != '{' && var5 != '[') {
                    validateBrackets(string, var3, var5);
                } else {
                    if (var3.isEmpty()) {
                        var1++;
                    }

                    var3.push(var5);
                }
            }
        }

        if (var2) {
            throw new NbtException("Unbalanced quotation: " + string);
        } else if (!var3.isEmpty()) {
            throw new NbtException("Unbalanced brackets: " + string);
        } else {
            return var1 == 0 && !string.isEmpty() ? 1 : var1;
        }
    }

    private static void validateBrackets(String string, Stack<Character> var3, char var5) {
        if (var5 == '}' && (var3.isEmpty() || var3.pop() != '{')) {
            throw new NbtException("Unbalanced curly brackets {}: " + string);
        }

        if (var5 == ']' && (var3.isEmpty() || var3.pop() != '[')) {
            throw new NbtException("Unbalanced square brackets []: " + string);
        }
    }

    static NbtParser method_7379(String string, String string2) {
        string2 = string2.trim();
        getTopElementCount(string2);
        if (string2.startsWith("{")) {
            if (!string2.endsWith("}")) {
                throw new NbtException("Unable to locate ending bracket for: " + string2);
            } else {
                string2 = string2.substring(1, string2.length() - 1);
                NbtCompoundParser var10 = new NbtCompoundParser(string);

                while (!string2.isEmpty()) {
                    String var11 = getFirstElement(string2, false);
                    if (!var11.isEmpty()) {
                        String var12 = getKey(var11, false);
                        String var13 = getValue(var11, false);
                        string2 = getString(string2, var11, var12, var13, var10.parsers);
                    }
                }

                return var10;
            }
        } else if (!string2.startsWith("[") || string2.matches("\\[[-\\d|,\\s]+]")) {
            return new NbtPrimitiveParser(string, string2);
        } else if (!string2.endsWith("]")) {
            throw new NbtException("Unable to locate ending bracket for: " + string2);
        } else {
            string2 = string2.substring(1, string2.length() - 1);
            NbtListParser var2 = new NbtListParser(string);

            while (!string2.isEmpty()) {
                String var3 = getFirstElement(string2, true);
                if (!var3.isEmpty()) {
                    String var4 = getKey(var3, true);
                    String var5 = getValue(var3, true);
                    string2 = getString(string2, var3, var4, var5, var2.parsers);
                    if (string2 == null) break;
                } else {
                    LOGGER.finer(string2);
                }
            }

            return var2;
        }
    }

    @Nullable
    private static String getString(String string2, String var3, String var4, String var5, ArrayList<NbtParser> parsers) {
        parsers.add(method_7379(var4, var5));
        if (string2.length() < var3.length() + 1) {
            return null;
        }

        char var6 = string2.charAt(var3.length());
        if (var6 != ',' && var6 != '{' && var6 != '}' && var6 != '[' && var6 != ']') {
            throw new NbtException("Unexpected token '" + var6 + "' at: " + string2.substring(var3.length()));
        }

        string2 = string2.substring(var3.length() + 1);
        return string2;
    }

    private static String getFirstElement(String stringNbt, boolean hasKey) {
        int var2 = indexOf(stringNbt, ':');
        if (var2 < 0 && !hasKey) {
            throw new NbtException("Unable to locate name/value separator for string: " + stringNbt);
        } else {
            int var3 = indexOf(stringNbt, ',');
            if (var3 >= 0 && var3 < var2 && !hasKey) {
                throw new NbtException("Name error at: " + stringNbt);
            } else {
                if (hasKey && (var2 < 0 || var2 > var3)) {
                    var2 = -1;
                }

                Stack<Character> var4 = new Stack<>();
                int var5 = var2 + 1;
                boolean var6 = false;
                boolean var7 = false;
                boolean var8 = false;

                for (int var9 = 0; var5 < stringNbt.length(); var5++) {
                    char var10 = stringNbt.charAt(var5);
                    if (var10 == '"') {
                        if (var5 > 0 && stringNbt.charAt(var5 - 1) == '\\') {
                            if (!var6) {
                                throw new NbtException("Illegal use of \\\": " + stringNbt);
                            }
                        } else {
                            var6 = !var6;
                            if (var6 && !var8) {
                                var7 = true;
                            }

                            if (!var6) {
                                var9 = var5;
                            }
                        }
                    } else if (!var6) {
                        if (var10 != '{' && var10 != '[') {
                            validateBrackets(stringNbt, var4, var10);

                            if (var10 == ',' && var4.isEmpty()) {
                                return stringNbt.substring(0, var5);
                            }
                        } else {
                            var4.push(var10);
                        }
                    }

                    if (!Character.isWhitespace(var10)) {
                        if (!var6 && var7 && var9 != var5) {
                            return stringNbt.substring(0, var9 + 1);
                        }

                        var8 = true;
                    }
                }

                return stringNbt.substring(0, var5);
            }
        }
    }

    private static String getKey(String stringNbt, boolean missingKey) {
        if (missingKey) {
            stringNbt = stringNbt.trim();
            if (stringNbt.startsWith("{") || stringNbt.startsWith("[")) {
                return "";
            }
        }

        int var2 = stringNbt.indexOf(58);
        if (var2 >= 0) {
            return stringNbt.substring(0, var2).trim();
        } else if (missingKey) {
            return "";
        } else {
            throw new NbtException("Unable to locate name/value separator for string: " + stringNbt);
        }
    }

    private static String getValue(String stringNbt, boolean missingKey) {
        if (missingKey) {
            stringNbt = stringNbt.trim();
            if (stringNbt.startsWith("{") || stringNbt.startsWith("[")) {
                return stringNbt;
            }
        }

        int var2 = stringNbt.indexOf(58);
        if (var2 >= 0) {
            return stringNbt.substring(var2 + 1).trim();
        } else if (missingKey) {
            return stringNbt;
        } else {
            throw new NbtException("Unable to locate name/value separator for string: " + stringNbt);
        }
    }

    private static int indexOf(String string, char c) {
        int var2 = 0;

        for (boolean var3 = false; var2 < string.length(); var2++) {
            char var4 = string.charAt(var2);
            if (var4 == '"') {
                if (var2 == 0 || string.charAt(var2 - 1) != '\\') {
                    var3 = !var3;
                }
            } else if (!var3) {
                if (var4 == c) {
                    return var2;
                }

                if (var4 == '{' || var4 == '[') {
                    return -1;
                }
            }
        }

        return -1;
    }
}
