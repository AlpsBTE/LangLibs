package li.cinnazeyy.langlibs.core.language;

import li.cinnazeyy.langlibs.core.config.ConfigUtil;
import li.cinnazeyy.langlibs.core.config.LanguageConfig;
import li.cinnazeyy.langlibs.core.config.LanguageSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public enum Language {
    en_GB,
    de_DE,
    fr_FR,
    ko_KR,
    pt_PT,
    ru_RU,
    zh_CN,
    zh_TW,
    he_IL,
    da_DK,
    es_ES,
    hu_HU,
    it_IT,
    nl_NL,
    ro_RO,
    cs_CZ,
    pl_PL,
    sk_SK,
    custom_1,
    custom_2,
    custom_3,
    custom_4,
    custom_5;

    private final String name, region, headId, itemModel;

    Language() {
        String name = "undefined";
        String region = "undefined";
        String headId = "36076";
        String itemModel = "";
        Logger logger = LoggerFactory.getLogger(Language.class);

        try {
            LanguageConfig config = ConfigUtil.getLanguageConfig();
            LanguageSection section = config == null ? null
                    : (LanguageSection) config.getClass().getMethod(this.name()).invoke(config);
            if (section == null) {
                logger.warn("Language {} is not defined in languages.yml!", this.name());
            } else {
                if (section.name() != null) name = section.name();
                if (section.region() != null) region = section.region();
                if (section.headId() != null) headId = section.headId();
                if (section.itemModel() != null) itemModel = section.itemModel();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.warn("Language {} is not defined in languages.yml! {}", this.name(), e.getMessage());
        }

        this.name = name;
        this.region = region;
        this.headId = headId;
        this.itemModel = itemModel;
    }

    public String getRegion() {
        return region;
    }

    public String getName() {
        return name;
    }

    public String getHeadId() {
        return headId;
    }

    public String getItemModel() {
        return itemModel;
    }
}


