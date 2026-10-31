package li.cinnazeyy.langui;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import li.cinnazeyy.langlibs.core.LangLibAPI;
import li.cinnazeyy.langlibs.core.event.LanguageChangeEvent;
import li.cinnazeyy.langlibs.core.language.Language;
import li.cinnazeyy.langui.util.LangUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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

import java.util.Objects;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class LanguageSelectMenu implements Listener {
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

        for (int i = 0; i < LangUtil.getInstance().languageFiles.length; i++) {
            Language lang = LangUtil.getInstance().languageFiles[i].getLanguage();
            menu.getSlot(i).setItem(getLanguageItem(lang));
            menu.getSlot(i).setClickHandler((p, info) -> clickLanguageItem(lang));
        }

        menu.getSlot(22).setItem(new ItemBuilder(Material.BARRIER)
                .setName(text(LangUtil.getInstance().get(menuPlayer, "menu.close"), RED)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false))
                .build());
        menu.getSlot(22).setClickHandler((p, info) -> menu.close(p));
        menu.open(menuPlayer);
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
        LanguageChangeEvent langEvent = new LanguageChangeEvent(menuPlayer, lang);
        menuPlayer.sendMessage(
                text(Objects.requireNonNull(LangUI.getPlugin().getConfig().getString("info-message-icon")))
                        .append(text(" » ", DARK_GRAY))
                        .append(text(LangUtil.getInstance().get(menuPlayer, "info.changed-language"), GREEN)));
        menuPlayer.playSound(menuPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        Bukkit.getPluginManager().callEvent(langEvent);
        menu.close(menuPlayer);
    }
}
