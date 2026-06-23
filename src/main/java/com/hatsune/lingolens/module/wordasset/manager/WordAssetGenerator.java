package com.hatsune.lingolens.module.wordasset.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.config.LingoLensLlmProperties;
import com.hatsune.lingolens.infrastructure.llm.LingoLensLlmClient;
import com.hatsune.lingolens.module.lens.bo.LensWordInfoBO;
import com.hatsune.lingolens.module.wordasset.bo.GeneratedWordAssetBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Generates a target-language card and a source-language explanation for one
 * stable word key. English object tags remain the canonical lookup key.
 *
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Slf4j
@Component
public class WordAssetGenerator {

    private final LingoLensLlmProperties llmProperties;
    private final LingoLensLlmClient llmClient;

    public WordAssetGenerator(LingoLensLlmProperties llmProperties,
                              LingoLensLlmClient llmClient) {
        this.llmProperties = llmProperties;
        this.llmClient = llmClient;
    }

    private static final Map<String, LocalizedSeed> LOCAL_WORDS = Map.ofEntries(
            entry("mug", seed("mug",
                    text("en", "Mug", "[mʌg]", "n.", "A cup with a handle, often used for hot drinks.", "She poured hot coffee into her ceramic mug."),
                    text("zh-CN", "马克杯", "[mǎ kè bēi]", "名词", "一种带把手、常用来喝热饮的杯子。", "她把热咖啡倒进陶瓷马克杯里。"),
                    text("ja", "マグカップ", "[magukappu]", "名詞", "取っ手のある、温かい飲み物に使うカップ。", "彼女は熱いコーヒーを陶器のマグカップに注いだ。"))),
            entry("keyboard", seed("keyboard",
                    text("en", "Keyboard", "[ˈkiːbɔːrd]", "n.", "A set of keys used to type into a computer.", "The mechanical keyboard clicks softly on the desk."),
                    text("zh-CN", "键盘", "[jiàn pán]", "名词", "用于向电脑输入文字或命令的一组按键。", "机械键盘在桌面上发出轻轻的敲击声。"),
                    text("ja", "キーボード", "[ki-bo-do]", "名詞", "コンピューターに文字や命令を入力するキーの集まり。", "機械式キーボードが机の上で軽く音を立てた。"))),
            entry("monitor", seed("monitor",
                    text("en", "Monitor", "[ˈmɑːnɪtər]", "n.", "A screen used to show information from a computer.", "The monitor displayed the design draft clearly."),
                    text("zh-CN", "显示器", "[xiǎn shì qì]", "名词", "用于显示电脑画面或信息的屏幕设备。", "显示器清晰地展示了设计草图。"),
                    text("ja", "モニター", "[monitaa]", "名詞", "コンピューターの情報を表示する画面。", "モニターにはデザイン案がはっきり表示された。"))),
            entry("laptop", seed("laptop",
                    text("en", "Laptop", "[ˈlæptɑːp]", "n.", "A portable computer that can be used on a desk or lap.", "Her laptop stayed open beside the notebook."),
                    text("zh-CN", "笔记本电脑", "[bǐ jì běn diàn nǎo]", "名词", "便于携带、可放在桌上或腿上使用的电脑。", "她的笔记本电脑一直打开在笔记本旁边。"),
                    text("ja", "ノートパソコン", "[nooto pasokon]", "名詞", "持ち運びできる小型のコンピューター。", "ノートパソコンはノートの横で開いたままだった。"))),
            entry("phone", seed("phone",
                    text("en", "Phone", "[foʊn]", "n.", "A device used for calls, messages, photos, and apps.", "He checked the photo on his phone."),
                    text("zh-CN", "手机", "[shǒu jī]", "名词", "用于通话、消息、拍照和运行应用的移动设备。", "他在手机上查看了那张照片。"),
                    text("ja", "スマートフォン", "[sumaato fon]", "名詞", "通話や写真、アプリに使う携帯端末。", "彼はスマートフォンで写真を確認した。"))),
            entry("pen", seed("pen",
                    text("en", "Pen", "[pen]", "n.", "A small tool used for writing with ink.", "She underlined the word with a black pen."),
                    text("zh-CN", "笔", "[bǐ]", "名词", "用墨水书写的小工具。", "她用黑色的笔在单词下面画线。"),
                    text("ja", "ペン", "[pen]", "名詞", "インクで文字を書くための小さな道具。", "彼女は黒いペンで単語に線を引いた。"))),
            entry("notebook", seed("notebook",
                    text("en", "Notebook", "[ˈnoʊtbʊk]", "n.", "A book of blank pages used for notes.", "The notebook was filled with new vocabulary."),
                    text("zh-CN", "笔记本", "[bǐ jì běn]", "名词", "用于记录内容的空白页本子。", "笔记本里写满了新词。"),
                    text("ja", "ノート", "[nooto]", "名詞", "メモを書くための紙の冊子。", "ノートには新しい単語がびっしり書かれていた。"))),
            entry("desk", seed("desk",
                    text("en", "Desk", "[desk]", "n.", "A table used for working, reading, or studying.", "A small lamp stood on the wooden desk."),
                    text("zh-CN", "书桌", "[shū zhuō]", "名词", "用于工作、阅读或学习的桌子。", "木质书桌上放着一盏小台灯。"),
                    text("ja", "机", "[tsukue]", "名詞", "仕事や勉強に使うテーブル。", "木の机の上に小さなランプが置かれていた。"))),
            entry("chair", seed("chair",
                    text("en", "Chair", "[tʃer]", "n.", "A seat with a back, usually for one person.", "The chair was pulled close to the desk."),
                    text("zh-CN", "椅子", "[yǐ zi]", "名词", "带靠背、通常供一人坐的座具。", "椅子被拉到书桌旁边。"),
                    text("ja", "椅子", "[isu]", "名詞", "一人で座るための背もたれ付きの家具。", "椅子は机の近くに引き寄せられていた。"))),
            entry("lamp", seed("lamp",
                    text("en", "Lamp", "[læmp]", "n.", "A device that gives light.", "The lamp made the page warm and clear."),
                    text("zh-CN", "台灯", "[tái dēng]", "名词", "放在桌面上用于照明的灯。", "台灯让页面显得温暖又清晰。"),
                    text("ja", "ランプ", "[ranpu]", "名詞", "明かりを出すための器具。", "ランプの光でページが暖かく見えた。"))),
            entry("book", seed("book",
                    text("en", "Book", "[bʊk]", "n.", "A set of printed or written pages bound together.", "The book was open to a chapter about travel."),
                    text("zh-CN", "书", "[shū]", "名词", "装订成册的印刷或书写页面。", "那本书翻到关于旅行的一章。"),
                    text("ja", "本", "[hon]", "名詞", "印刷または書かれたページをまとめたもの。", "本は旅行についての章で開かれていた。"))),
            entry("bottle", seed("bottle",
                    text("en", "Bottle", "[ˈbɑːtl]", "n.", "A container with a narrow neck for holding liquids.", "A water bottle sat beside the laptop."),
                    text("zh-CN", "瓶子", "[píng zi]", "名词", "带窄口、用于盛放液体的容器。", "一个水瓶放在笔记本电脑旁边。"),
                    text("ja", "ボトル", "[botoru]", "名詞", "液体を入れる細い口の容器。", "水のボトルがノートパソコンの横に置かれていた。"))),
            entry("cup", seed("cup",
                    text("en", "Cup", "[kʌp]", "n.", "A small container used for drinking.", "The cup left a faint ring on the table."),
                    text("zh-CN", "杯子", "[bēi zi]", "名词", "用于喝饮品的小容器。", "杯子在桌面上留下淡淡的圆痕。"),
                    text("ja", "カップ", "[kappu]", "名詞", "飲み物を飲むための小さな容器。", "カップはテーブルに薄い輪の跡を残した。"))),
            entry("bag", seed("bag",
                    text("en", "Bag", "[bæɡ]", "n.", "A flexible container used to carry things.", "The bag was resting near the chair."),
                    text("zh-CN", "包", "[bāo]", "名词", "用于携带物品的柔性容器。", "包放在椅子旁边。"),
                    text("ja", "バッグ", "[baggu]", "名詞", "物を持ち運ぶための柔らかい入れ物。", "バッグは椅子の近くに置かれていた。"))),
            entry("wallet", seed("wallet",
                    text("en", "Wallet", "[ˈwɑːlɪt]", "n.", "A small case used to carry money and cards.", "He put the receipt back into his wallet."),
                    text("zh-CN", "钱包", "[qián bāo]", "名词", "用于放钱和卡片的小夹包。", "他把收据放回钱包里。"),
                    text("ja", "財布", "[saifu]", "名詞", "お金やカードを入れる小さな入れ物。", "彼はレシートを財布に戻した。"))),
            entry("watch", seed("watch",
                    text("en", "Watch", "[wɑːtʃ]", "n.", "A small clock worn on the wrist.", "Her watch showed that the meeting would start soon."),
                    text("zh-CN", "手表", "[shǒu biǎo]", "名词", "戴在手腕上的小型计时器。", "她的手表显示会议快开始了。"),
                    text("ja", "腕時計", "[udedokei]", "名詞", "手首につける小さな時計。", "彼女の腕時計は会議がもうすぐ始まることを示していた。"))),
            entry("glasses", seed("glasses",
                    text("en", "Glasses", "[ˈɡlæsɪz]", "n.", "A pair of lenses worn to help vision.", "His glasses were folded beside the book."),
                    text("zh-CN", "眼镜", "[yǎn jìng]", "名词", "戴在眼前、帮助视力的一副镜片。", "他的眼镜折好放在书旁边。"),
                    text("ja", "眼鏡", "[megane]", "名詞", "視力を助けるために目の前につけるレンズ。", "彼の眼鏡は本の横に折りたたまれていた。"))),
            entry("window", seed("window",
                    text("en", "Window", "[ˈwɪndoʊ]", "n.", "An opening in a wall or vehicle that lets in light.", "Soft morning light came through the window."),
                    text("zh-CN", "窗户", "[chuāng hu]", "名词", "墙上或车辆上让光线进入的开口。", "柔和的晨光从窗户照进来。"),
                    text("ja", "窓", "[mado]", "名詞", "光や空気を入れるための壁や乗り物の開口部。", "柔らかな朝の光が窓から差し込んだ。"))),
            entry("camera", seed("camera",
                    text("en", "Camera", "[ˈkæmərə]", "n.", "A device used to take photos or videos.", "The camera focused on the object automatically."),
                    text("zh-CN", "相机", "[xiàng jī]", "名词", "用于拍摄照片或视频的设备。", "相机自动对准了物体。"),
                    text("ja", "カメラ", "[kamera]", "名詞", "写真や動画を撮るための機器。", "カメラは自動で物体に焦点を合わせた。"))),
            entry("headphones", seed("headphones",
                    text("en", "Headphones", "[ˈhedfoʊnz]", "n.", "A device worn over the ears for listening to sound.", "The headphones played the pronunciation clearly."),
                    text("zh-CN", "耳机", "[ěr jī]", "名词", "戴在耳朵上用于听声音的设备。", "耳机清楚地播放了发音。"),
                    text("ja", "ヘッドホン", "[heddohon]", "名詞", "耳につけて音を聞くための機器。", "ヘッドホンから発音がはっきり聞こえた。"))),
            entry("charger", seed("charger",
                    text("en", "Charger", "[ˈtʃɑːrdʒər]", "n.", "A device used to restore power to a battery.", "The charger was plugged in behind the desk."),
                    text("zh-CN", "充电器", "[chōng diàn qì]", "名词", "用于给电池恢复电量的设备。", "充电器插在书桌后面。"),
                    text("ja", "充電器", "[juudenki]", "名詞", "電池に電力を戻すための機器。", "充電器は机の後ろに差し込まれていた。"))),
            entry("plate", seed("plate",
                    text("en", "Plate", "[pleɪt]", "n.", "A flat dish used for serving food.", "A white plate sat next to the cup."),
                    text("zh-CN", "盘子", "[pán zi]", "名词", "用于盛放食物的扁平餐具。", "一个白色盘子放在杯子旁边。"),
                    text("ja", "皿", "[sara]", "名詞", "食べ物をのせる平たい食器。", "白い皿がカップの横に置かれていた。"))),
            entry("spoon", seed("spoon",
                    text("en", "Spoon", "[spuːn]", "n.", "A utensil with a small bowl-shaped end.", "The spoon rested inside the cup."),
                    text("zh-CN", "勺子", "[sháo zi]", "名词", "一端呈小碗状的餐具。", "勺子放在杯子里面。"),
                    text("ja", "スプーン", "[supuun]", "名詞", "先が小さなくぼみになった食器。", "スプーンはカップの中に入っていた。"))),
            entry("umbrella", seed("umbrella",
                    text("en", "Umbrella", "[ʌmˈbrelə]", "n.", "A folding cover used for protection from rain or sun.", "The wet umbrella leaned against the wall."),
                    text("zh-CN", "雨伞", "[yǔ sǎn]", "名词", "用于遮雨或遮阳的可折叠工具。", "湿雨伞靠在墙边。"),
                    text("ja", "傘", "[kasa]", "名詞", "雨や日差しを避けるための折りたたみ式の道具。", "濡れた傘が壁に立てかけられていた。"))),
            entry("menu", seed("menu",
                    text("en", "Menu", "[ˈmenjuː]", "n.", "A list of food or drink choices.", "The menu listed several seasonal drinks."),
                    text("zh-CN", "菜单", "[cài dān]", "名词", "列出食物或饮品选择的清单。", "菜单上列着几款季节饮品。"),
                    text("ja", "メニュー", "[menyuu]", "名詞", "食べ物や飲み物の選択肢を示す一覧。", "メニューには季節限定の飲み物がいくつか載っていた。"))),
            entry("ticket", seed("ticket",
                    text("en", "Ticket", "[ˈtɪkɪt]", "n.", "A pass that allows entry or travel.", "She kept the ticket inside her phone case."),
                    text("zh-CN", "票", "[piào]", "名词", "用于入场或乘车的凭证。", "她把票夹在手机壳里。"),
                    text("ja", "チケット", "[chiketto]", "名詞", "入場や移動のための証明となる券。", "彼女はチケットをスマートフォンケースに入れていた。"))),
            entry("train", seed("train",
                    text("en", "Train", "[treɪn]", "n.", "A connected set of railway cars pulled along tracks.", "The train arrived quietly at the platform."),
                    text("zh-CN", "火车", "[huǒ chē]", "名词", "沿轨道行驶、由多节车厢组成的交通工具。", "火车安静地驶入站台。"),
                    text("ja", "電車", "[densha]", "名詞", "線路の上を走る複数の車両からなる乗り物。", "電車は静かにホームへ入ってきた。"))),
            entry("station", seed("station",
                    text("en", "Station", "[ˈsteɪʃn]", "n.", "A place where trains or buses stop for passengers.", "The station was bright even late at night."),
                    text("zh-CN", "车站", "[chē zhàn]", "名词", "火车或公交停靠并上下乘客的地方。", "即使深夜，车站依然很明亮。"),
                    text("ja", "駅", "[eki]", "名詞", "電車やバスが乗客のために止まる場所。", "夜遅くても駅は明るかった。"))),
            entry("coffee", seed("coffee",
                    text("en", "Coffee", "[ˈkɔːfi]", "n.", "A dark drink made from roasted coffee beans.", "The smell of coffee filled the cafe."),
                    text("zh-CN", "咖啡", "[kā fēi]", "名词", "由烘焙咖啡豆制成的深色饮品。", "咖啡的香气充满了咖啡馆。"),
                    text("ja", "コーヒー", "[koohii]", "名詞", "焙煎したコーヒー豆から作る濃い飲み物。", "コーヒーの香りがカフェに広がっていた。"))),
            entry("pastry", seed("pastry",
                    text("en", "Pastry", "[ˈpeɪstri]", "n.", "A baked food made with flour and fat.", "A flaky pastry sat beside the coffee."),
                    text("zh-CN", "酥皮点心", "[sū pí diǎn xīn]", "名词", "由面粉和油脂烘烤制成的点心。", "一块酥皮点心放在咖啡旁边。"),
                    text("ja", "ペストリー", "[pesutorii]", "名詞", "小麦粉と油脂で焼いた菓子やパン。", "サクサクしたペストリーがコーヒーの横にあった。")))
    );

    public LensWordInfoBO generate(String word, String sourceLanguage, String targetLanguage) {
        LensWordInfoBO localWord = generateLocalSeed(word, sourceLanguage, targetLanguage);
        if (localWord != null) {
            return localWord;
        }
        String wordKey = normalizeKey(word);
        if (llmClient.available()) {
            LensWordInfoBO llmWord = generateByLlm(wordKey, sourceLanguage, targetLanguage);
            if (llmWord != null) {
                return llmWord;
            }
        }
        return fallbackWord(wordKey, sourceLanguage, targetLanguage);
    }

    public LensWordInfoBO generateEnriched(String word, String sourceLanguage, String targetLanguage) {
        String wordKey = normalizeKey(word);
        if (llmClient.available()) {
            LensWordInfoBO llmWord = generateByLlm(wordKey, sourceLanguage, targetLanguage);
            if (llmWord != null) {
                return llmWord;
            }
        }
        return generateFastSeed(wordKey, sourceLanguage, targetLanguage);
    }

    public GeneratedWordAssetBO generateFromImage(String imageUrl) {
        if (!llmClient.available() || !StringUtils.hasText(imageUrl)) {
            return null;
        }
        try {
            JSONObject generated = llmClient.completeJson(
                    imageSystemPrompt(),
                    imageUserPrompt(),
                    imageUrl
            );
            if (generated == null || generated.isEmpty()) {
                return null;
            }
            GeneratedWordAssetBO result = toGeneratedAsset(generated);
            return result == null || !StringUtils.hasText(result.getWordKey()) ? null : result;
        } catch (Exception e) {
            log.warn("word asset image llm generation failed, imageUrl={}", imageUrl, e);
            return null;
        }
    }

    public LensWordInfoBO generateLocalSeed(String word, String sourceLanguage, String targetLanguage) {
        String wordKey = normalizeKey(word);
        LocalizedSeed seed = LOCAL_WORDS.get(wordKey);
        return seed == null ? null : localizeSeed(seed, sourceLanguage, targetLanguage);
    }

    public LensWordInfoBO generateFastSeed(String word, String sourceLanguage, String targetLanguage) {
        LensWordInfoBO localSeed = generateLocalSeed(word, sourceLanguage, targetLanguage);
        if (localSeed != null) {
            return localSeed;
        }
        return fallbackWord(normalizeKey(word), sourceLanguage, targetLanguage);
    }

    private LensWordInfoBO localizeSeed(LocalizedSeed seed, String sourceLanguage, String targetLanguage) {
        LocalizedText target = seed.textFor(targetLanguage);
        LocalizedText source = seed.textFor(sourceLanguage);
        return LensWordInfoBO.builder()
                .word(seed.wordKey())
                .displayWord(target.displayWord())
                .phonetic(target.phonetic())
                .partOfSpeech(target.partOfSpeech())
                .translation(source.displayWord())
                .definition(source.definition())
                .exampleSentence(target.exampleSentence())
                .exampleTranslation(source.exampleSentence())
                .build();
    }

    private LensWordInfoBO fallbackWord(String wordKey, String sourceLanguage, String targetLanguage) {
        String displayWord = toDisplayWord(wordKey);
        String targetKey = languageKey(targetLanguage);
        String sourceKey = languageKey(sourceLanguage);
        return LensWordInfoBO.builder()
                .word(wordKey)
                .displayWord(displayWord)
                .phonetic(defaultPhonetic(targetKey, displayWord))
                .partOfSpeech(partOfSpeechFor(targetKey))
                .translation(displayWord)
                .definition(defaultDefinition(sourceKey))
                .exampleSentence(defaultExample(targetKey, wordKey, displayWord))
                .exampleTranslation(defaultExample(sourceKey, wordKey, displayWord))
                .build();
    }

    private LensWordInfoBO generateByLlm(String wordKey, String sourceLanguage, String targetLanguage) {
        try {
            JSONObject generated = llmClient.completeJson(
                    wordSystemPrompt(),
                    """
                            Word key: %s
                            Source language: %s
                            Target language: %s

                            Return exactly these JSON fields:
                            word, displayWord, phonetic, partOfSpeech, translation, definition, exampleSentence, exampleTranslation.
                            
                            Reminder:
                            No prose. No markdown. No explanations. The first output character must be "{".
                            """.formatted(wordKey, sourceLanguage, targetLanguage)
            );
            if (generated == null) {
                return null;
            }
            return LensWordInfoBO.builder()
                    .word(defaultText(generated.getString("word"), wordKey))
                    .displayWord(defaultText(generated.getString("displayWord"), toDisplayWord(wordKey)))
                    .phonetic(defaultText(generated.getString("phonetic"), defaultPhonetic(languageKey(targetLanguage), toDisplayWord(wordKey))))
                    .partOfSpeech(defaultText(generated.getString("partOfSpeech"), partOfSpeechFor(languageKey(targetLanguage))))
                    .translation(defaultText(generated.getString("translation"), toDisplayWord(wordKey)))
                    .definition(defaultText(generated.getString("definition"), defaultDefinition(languageKey(sourceLanguage))))
                    .exampleSentence(defaultText(generated.getString("exampleSentence"),
                            defaultExample(languageKey(targetLanguage), wordKey, toDisplayWord(wordKey))))
                    .exampleTranslation(defaultText(generated.getString("exampleTranslation"),
                            defaultExample(languageKey(sourceLanguage), wordKey, toDisplayWord(wordKey))))
                    .build();
        } catch (Exception e) {
            log.warn("word asset llm generation failed, wordKey={}", wordKey, e);
            return null;
        }
    }

    private GeneratedWordAssetBO toGeneratedAsset(JSONObject generated) {
        List<GeneratedWordAssetBO.LanguagePayload> payloads = new ArrayList<>();
        for (LingoLensLlmProperties.LanguageSpec language : llmProperties.getLanguages()) {
            if (language == null || !StringUtils.hasText(language.getCode())) {
                continue;
            }
            String responseKey = StringUtils.hasText(language.getResponseKey())
                    ? language.getResponseKey().trim()
                    : "item_" + contentLanguage(language.getCode());
            JSONObject item = generated.getJSONObject(responseKey);
            if (item == null) {
                item = generated.getJSONObject(contentLanguage(language.getCode()));
            }
            if (item == null || !StringUtils.hasText(item.getString("word"))) {
                continue;
            }
            payloads.add(GeneratedWordAssetBO.LanguagePayload.builder()
                    .lang(contentLanguage(language.getCode()))
                    .word(item.getString("word").trim())
                    .phonetic(firstText(item, "phonetic", "pronunciation"))
                    .pos(defaultText(firstText(item, "pos", "partOfSpeech", "part_of_speech"), partOfSpeechFor(contentLanguage(language.getCode()))))
                    .explanation(firstText(item, "explanation", "definition", "translation"))
                    .example(firstText(item, "example", "exampleSentence", "example_sentence"))
                    .build());
        }
        if (payloads.isEmpty()) {
            return null;
        }
        String canonicalWord = payloads.stream()
                .filter(payload -> "en".equals(contentLanguage(payload.getLang())))
                .findFirst()
                .orElse(payloads.get(0))
                .getWord();
        String wordKey = normalizeKey(defaultText(generated.getString("word_key"), canonicalWord));
        return GeneratedWordAssetBO.builder()
                .wordKey(wordKey)
                .canonicalLanguage("en")
                .canonicalWord(canonicalWord)
                .payloads(payloads)
                .build();
    }

    private String imageSystemPrompt() {
        if (StringUtils.hasText(llmProperties.getImageSystemPrompt())) {
            return llmProperties.getImageSystemPrompt();
        }
        return """
                你是一个语言学习类应用的后端图像识别助手。
                分析用户上传的图片，精准识别图片中央或最核心的物体。
                请直接返回 JSON 对象，不要包含任何 Markdown 标记。
                每个语言对象必须包含 word, phonetic, pos, explanation, example 字段。
                英文 phonetic 使用 IPA；中文 phonetic 使用拼音；日文 phonetic 使用罗马音。
                JSON 结构示例：
                {
                  "item_zh": {"word":"苹果","phonetic":"píng guǒ","pos":"名词","explanation":"一种常见水果。","example":"桌子上有一个红色的苹果。"},
                  "item_en": {"word":"apple","phonetic":"/ˈæp.əl/","pos":"noun","explanation":"a common fruit","example":"There is a red apple on the table."},
                  "item_ja": {"word":"リンゴ","phonetic":"ringo","pos":"名詞","explanation":"一般的な果物。","example":"机の上に赤いリンゴがあります。"}
                }
                """;
    }

    private String imageUserPrompt() {
        return """
                Identify the central object in the image and return one JSON object.
                Required language keys are configured as: %s.
                Keep the result concise and suitable for a mobile vocabulary flash card.
                """.formatted(llmProperties.getLanguages().stream()
                .map(language -> language.getResponseKey() + "=" + language.getCode())
                .toList());
    }

    private String wordSystemPrompt() {
        if (StringUtils.hasText(llmProperties.getWordSystemPrompt())) {
            return llmProperties.getWordSystemPrompt();
        }
        return """
                You are MiniMax-M3 acting as a senior trilingual lexicographer for a language learning app.
                Return one raw JSON object only. No markdown, no prose, no chain-of-thought.

                Required fields:
                word, displayWord, phonetic, partOfSpeech, translation, definition, exampleSentence, exampleTranslation.

                Rules:
                - word must remain a stable lowercase English object key when possible.
                - displayWord, phonetic, partOfSpeech, and exampleSentence use the target language.
                - translation, definition, and exampleTranslation use the source/native language.
                - If target language is English, phonetic must be standard IPA.
                - If target language is Chinese, phonetic must be pinyin with tone marks.
                - If target language is Japanese, phonetic must include kana plus romaji, for example "ボトル / botoru".
                - definition is a professional concise meaning, not a casual one-word translation.
                - exampleSentence must be natural, short, and object-scene oriented.
                - Keep the result suitable for a mobile vocabulary card.
                """;
    }

    private String firstText(JSONObject item, String... keys) {
        for (String key : keys) {
            String value = item.getString(key);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String normalizeKey(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }

    private String languageKey(String language) {
        return languageKeyOf(language);
    }

    private String contentLanguage(String language) {
        String key = languageKeyOf(language);
        return "zh-CN".equals(key) ? "zh" : key;
    }

    private String toDisplayWord(String word) {
        if (word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
    }

    private String defaultPhonetic(String languageKey, String displayWord) {
        if ("ja".equals(languageKey)) {
            return "[" + displayWord.toLowerCase(Locale.ROOT) + "]";
        }
        return "[" + displayWord + "]";
    }

    private String partOfSpeechFor(String languageKey) {
        return switch (languageKey) {
            case "zh-CN" -> "名词";
            case "ja" -> "名詞";
            default -> "n.";
        };
    }

    private String defaultDefinition(String languageKey) {
        return switch (languageKey) {
            case "zh-CN" -> "从扫描场景中识别出的可见物品。";
            case "ja" -> "スキャンした場面から認識された身近な物。";
            default -> "A visible object identified from the scanned scene.";
        };
    }

    private String defaultExample(String languageKey, String wordKey, String displayWord) {
        return switch (languageKey) {
            case "zh-CN" -> "我在这个场景里注意到了" + displayWord + "。";
            case "ja" -> "この場面で" + displayWord + "を見つけた。";
            default -> "I noticed the " + wordKey + " in the scene.";
        };
    }

    private static LocalizedSeed seed(String wordKey, LocalizedText english, LocalizedText chinese, LocalizedText japanese) {
        return new LocalizedSeed(wordKey, Map.of(
                "en", english,
                "zh-CN", chinese,
                "ja", japanese
        ));
    }

    private static LocalizedText text(String language,
                                      String displayWord,
                                      String phonetic,
                                      String partOfSpeech,
                                      String definition,
                                      String exampleSentence) {
        return new LocalizedText(language, displayWord, phonetic, partOfSpeech, definition, exampleSentence);
    }

    private record LocalizedSeed(String wordKey, Map<String, LocalizedText> texts) {
        private LocalizedText textFor(String language) {
            String key = languageKeyOf(language);
            return texts.getOrDefault(key, texts.get("en"));
        }
    }

    private record LocalizedText(String language,
                                 String displayWord,
                                 String phonetic,
                                 String partOfSpeech,
                                 String definition,
                                 String exampleSentence) {
    }

    private static String languageKeyOf(String language) {
        if (!StringUtils.hasText(language)) {
            return "en";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("zh")) {
            return "zh-CN";
        }
        if (normalized.startsWith("ja")) {
            return "ja";
        }
        return "en";
    }
}
