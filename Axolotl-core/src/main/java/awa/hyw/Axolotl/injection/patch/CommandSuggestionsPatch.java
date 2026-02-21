package awa.hyw.Axolotl.injection.patch;

import awa.hyw.Axolotl.command.CommandManager;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;
import awa.hyw.patchify.asm.ReflectionUtil;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import awa.hyw.patchify.CallbackInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Patch(CommandSuggestions.class)
public class CommandSuggestionsPatch {

    @Inject(method = "updateCommandInfo", desc = "()V")
    public static void updateCommandInfo(CommandSuggestions instance, CallbackInfo ci) {
        try {
            String className = "net.minecraft.client.gui.components.CommandSuggestions";
            
            // Get input field using ReflectionUtil
            // f_93853_ is the obfuscated name for 'input'
            Object inputObj = ReflectionUtil.getField(instance, "f_93853_", className);
            if (!(inputObj instanceof EditBox)) return;
            EditBox input = (EditBox) inputObj;
            
            String text = input.getValue();

            if (text.startsWith(".")) {
                ci.cancel();

                int cursor = input.getCursorPosition();
                
                List<String> suggestionsList = CommandManager.INSTANCE.getSuggestions(text);
                
                if (suggestionsList.isEmpty()) {
                     // Set pendingSuggestions to null
                     // f_93865_ is the obfuscated name for 'pendingSuggestions'
                     ReflectionUtil.setField(instance, null, "f_93865_", className);
                     return;
                }

                // Calculate range
                int lastSpace = text.lastIndexOf(' ', cursor - 1);
                int start = lastSpace + 1;
                // if we are at the start of the command (e.g. ".sk"), we want to replace from index 1 (skip '.')
                // But typically suggestions are for arguments. 
                // However, for the command itself:
                if (start == 0 && text.startsWith(".")) {
                    start = 0; // Include the dot for replacement if we are suggesting commands like ".skin"
                }
                
                StringRange range = StringRange.between(start, cursor);

                List<Suggestion> suggestions = new ArrayList<>();
                for (String s : suggestionsList) {
                    suggestions.add(new Suggestion(range, s, null));
                }

                Suggestions suggestionsObj = new Suggestions(range, suggestions);
                
                // Set pendingSuggestions
                ReflectionUtil.setField(instance, CompletableFuture.completedFuture(suggestionsObj), "f_93865_", className);
                
                // Invoke showSuggestions(boolean)
                // m_93930_ is the obfuscated name for 'showSuggestions'
                try {
                    Method showMethod;
                    try {
                        showMethod = instance.getClass().getDeclaredMethod("m_93930_", boolean.class);
                    } catch (NoSuchMethodException e) {
                        showMethod = instance.getClass().getDeclaredMethod("showSuggestions", boolean.class);
                    }
                    showMethod.setAccessible(true);
                    showMethod.invoke(instance, false);
                } catch (Exception e) {
                     e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
