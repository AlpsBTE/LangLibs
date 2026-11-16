package li.cinnazeyy.langui;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import li.cinnazeyy.langlibs.core.LangLibAPI;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
import li.cinnazeyy.langlibs.core.language.Language;
import li.cinnazeyy.langui.util.LangUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class LanguageSelectMenu implements Listener {
    private static final String SHOWN_LANGUAGES_PATH = "shown-languages";
    private static final int MAX_SHOWN_LANGUAGES = 18;
    private static final int CLOSE_SLOT = 22;

    private final Menu menu;
    private final Player menuPlayer;

    public LanguageSelectMenu(@NotNull Player player) {
        menuPlayer = player;
        menu = ChestMenu.builder(3)
                .title(LangUtil.getInstance().getComponent(player.getUniqueId(), "menu.select-language", BLACK))
                .build();
    }

    public void open() {
        Mask mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(empty()).build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
        mask.apply(menu);

        List<Language> shownLanguages = getShownLanguages();
        for (int i = 0; i < shownLanguages.size(); i++) {
            Language lang = shownLanguages.get(i);
            menu.getSlot(i).setItem(getLanguageItem(lang));
            menu.getSlot(i).setClickHandler((p, info) -> clickLanguageItem(lang));
        }

        menu.getSlot(CLOSE_SLOT).setItem(new ItemBuilder(Material.BARRIER)
                .setName(text(LangUtil.getInstance().get(menuPlayer, "menu.close"), RED)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false))
                .build());
        menu.getSlot(CLOSE_SLOT).setClickHandler((p, info) -> menu.close(p));
        menu.open(menuPlayer);
    }

    private List<Language> getShownLanguages() {
        LanguageFile[] languageFiles = LangUtil.getInstance().languageFiles;
        List<Language> registeredLanguages = Arrays.stream(languageFiles)
                .map(LanguageFile::getLanguage)
                .toList();
        List<String> shownLanguageIds = LangUI.getPlugin().getConfig().getStringList(SHOWN_LANGUAGES_PATH);

        if (shownLanguageIds.isEmpty()) return registeredLanguages;

        Set<Language> shownLanguages = new LinkedHashSet<>();
        for (String languageId : shownLanguageIds) {
            if (languageId == null || languageId.isBlank()) continue;

            String trimmedId = languageId.trim();
            Language language = Arrays.stream(Language.values())
                    .filter(lang -> lang.name().equalsIgnoreCase(trimmedId))
                    .findFirst()
                    .orElse(null);

            if (language == null) {
                LangUI.getPlugin().getLogger().warning("Unknown language '" + trimmedId + "' in " + SHOWN_LANGUAGES_PATH + "; skipping it.");
            } else if (!registeredLanguages.contains(language)) {
                LangUI.getPlugin().getLogger().warning("Language '" + trimmedId + "' in " + SHOWN_LANGUAGES_PATH + " is not registered; skipping it.");
            } else shownLanguages.add(language);
        }

        if (shownLanguages.isEmpty()) {
            LangUI.getPlugin().getLogger().warning("No valid languages in " + SHOWN_LANGUAGES_PATH + "; showing all languages instead.");
            return registeredLanguages;
        }

        List<Language> result = new ArrayList<>(shownLanguages);
        if (result.size() > MAX_SHOWN_LANGUAGES) {
            LangUI.getPlugin().getLogger().warning(SHOWN_LANGUAGES_PATH + " contains more than " + MAX_SHOWN_LANGUAGES + " languages; only the first " + MAX_SHOWN_LANGUAGES + " will be shown.");
            result = result.subList(0, MAX_SHOWN_LANGUAGES);
        }
        return result;
    }

    private ItemStack getLanguageItem(@NotNull Language lang) {
        boolean useHeads = LangUI.getPlugin().getConfig().getBoolean("languageSelection.useHeads");

        TextComponent itemName = text(lang.getName(), GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
                .append(text(" (", DARK_GRAY))
                .append(text(lang.getRegion(), GRAY))
                .append(text(")", DARK_GRAY));

        return useHeads ?
                new ItemBuilder(LangUI.getPlugin().getHeadLookupService().getCustomHead(lang.getHeadId())).setName(itemName).build()
                : new ItemBuilder(Material.PAPER).setItemModel(lang.getItemModel()).setName(itemName).build();
    }

    private void clickLanguageItem(Language lang) {
        LangLibAPI.setPlayerLang(menuPlayer, lang.toString());
        menuPlayer.sendMessage(
                text(Objects.requireNonNull(LangUI.getPlugin().getConfig().getString("info-message-icon")))
                        .append(text(" » ", DARK_GRAY))
                        .append(text(LangUtil.getInstance().get(lang, "info.changed-language"), GREEN)));
        menuPlayer.playSound(menuPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        menu.close(menuPlayer);
    }
}
