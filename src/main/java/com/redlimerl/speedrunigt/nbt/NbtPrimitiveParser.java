package com.redlimerl.speedrunigt.nbt;

import net.minecraft.nbt.*;

class NbtPrimitiveParser extends NbtParser {
    protected String value;

    public NbtPrimitiveParser(String string, String string2) {
        this.key = string;
        this.value = string2;
    }

    @Override
    public NbtElement parse() {
        try {
            if (this.value.matches("[-+]?[0-9]*\\.?[0-9]+[d|D]")) {
                return new NbtDouble(null, Double.parseDouble(this.value.substring(0, this.value.length() - 1)));
            } else if (this.value.matches("[-+]?[0-9]*\\.?[0-9]+[f|F]")) {
                return new NbtFloat(null, Float.parseFloat(this.value.substring(0, this.value.length() - 1)));
            } else if (this.value.matches("[-+]?[0-9]+[b|B]")) {
                return new NbtByte(null, Byte.parseByte(this.value.substring(0, this.value.length() - 1)));
            } else if (this.value.matches("[-+]?[0-9]+[l|L]")) {
                return new NbtLong(null, Long.parseLong(this.value.substring(0, this.value.length() - 1)));
            } else if (this.value.matches("[-+]?[0-9]+[s|S]")) {
                return new NbtShort(null, Short.parseShort(this.value.substring(0, this.value.length() - 1)));
            } else if (this.value.matches("[-+]?[0-9]+")) {
                return new NbtInt(null, Integer.parseInt(this.value));
            } else if (this.value.matches("[-+]?[0-9]*\\.?[0-9]+")) {
                return new NbtDouble(null, Double.parseDouble(this.value));
            } else if (this.value.equalsIgnoreCase("true") || this.value.equalsIgnoreCase("false")) {
                return new NbtByte(null, (byte) (Boolean.parseBoolean(this.value) ? 1 : 0));
            } else if (this.value.startsWith("[") && this.value.endsWith("]")) {
                if (this.value.length() > 2) {
                    String var1 = this.value.substring(1, this.value.length() - 1);
                    String[] var2 = var1.split(",");

                    try {
                        if (var2.length <= 1) {
                            return new NbtIntArray(null, new int[]{Integer.parseInt(var1.trim())});
                        } else {
                            int[] var3 = new int[var2.length];

                            for (int var4 = 0; var4 < var2.length; var4++) {
                                var3[var4] = Integer.parseInt(var2[var4].trim());
                            }

                            return new NbtIntArray(null, var3);
                        }
                    } catch (NumberFormatException var5) {
                        return new NbtString(this.value);
                    }
                } else {
                    return new NbtIntArray(null);
                }
            } else {
                if (this.value.startsWith("\"") && this.value.endsWith("\"") && this.value.length() > 2) {
                    this.value = this.value.substring(1, this.value.length() - 1);
                }

                this.value = this.value.replaceAll("\\\\\"", "\"");
                return new NbtString(this.value);
            }
        } catch (NumberFormatException var6) {
            this.value = this.value.replaceAll("\\\\\"", "\"");
            return new NbtString(this.value);
        }
    }
}
