package com.hatsune.lingolens.module.lens.manager;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class GroundingPhoneticService {

    private static final Map<String, String> KANA_DIGRAPHS = Map.ofEntries(
            Map.entry("きゃ", "kya"), Map.entry("きゅ", "kyu"), Map.entry("きょ", "kyo"),
            Map.entry("しゃ", "sha"), Map.entry("しゅ", "shu"), Map.entry("しょ", "sho"),
            Map.entry("ちゃ", "cha"), Map.entry("ちゅ", "chu"), Map.entry("ちょ", "cho"),
            Map.entry("にゃ", "nya"), Map.entry("にゅ", "nyu"), Map.entry("にょ", "nyo"),
            Map.entry("ひゃ", "hya"), Map.entry("ひゅ", "hyu"), Map.entry("ひょ", "hyo"),
            Map.entry("みゃ", "mya"), Map.entry("みゅ", "myu"), Map.entry("みょ", "myo"),
            Map.entry("りゃ", "rya"), Map.entry("りゅ", "ryu"), Map.entry("りょ", "ryo"),
            Map.entry("ぎゃ", "gya"), Map.entry("ぎゅ", "gyu"), Map.entry("ぎょ", "gyo"),
            Map.entry("じゃ", "ja"), Map.entry("じゅ", "ju"), Map.entry("じょ", "jo"),
            Map.entry("びゃ", "bya"), Map.entry("びゅ", "byu"), Map.entry("びょ", "byo"),
            Map.entry("ぴゃ", "pya"), Map.entry("ぴゅ", "pyu"), Map.entry("ぴょ", "pyo"),
            Map.entry("ふぁ", "fa"), Map.entry("ふぃ", "fi"), Map.entry("ふぇ", "fe"), Map.entry("ふぉ", "fo"),
            Map.entry("てぃ", "ti"), Map.entry("でぃ", "di"), Map.entry("とぅ", "tu"), Map.entry("どぅ", "du"),
            Map.entry("うぃ", "wi"), Map.entry("うぇ", "we"), Map.entry("うぉ", "wo")
    );

    private static final Map<Character, String> KANA = Map.ofEntries(
            Map.entry('あ', "a"), Map.entry('い', "i"), Map.entry('う', "u"), Map.entry('え', "e"), Map.entry('お', "o"),
            Map.entry('か', "ka"), Map.entry('き', "ki"), Map.entry('く', "ku"), Map.entry('け', "ke"), Map.entry('こ', "ko"),
            Map.entry('さ', "sa"), Map.entry('し', "shi"), Map.entry('す', "su"), Map.entry('せ', "se"), Map.entry('そ', "so"),
            Map.entry('た', "ta"), Map.entry('ち', "chi"), Map.entry('つ', "tsu"), Map.entry('て', "te"), Map.entry('と', "to"),
            Map.entry('な', "na"), Map.entry('に', "ni"), Map.entry('ぬ', "nu"), Map.entry('ね', "ne"), Map.entry('の', "no"),
            Map.entry('は', "ha"), Map.entry('ひ', "hi"), Map.entry('ふ', "fu"), Map.entry('へ', "he"), Map.entry('ほ', "ho"),
            Map.entry('ま', "ma"), Map.entry('み', "mi"), Map.entry('む', "mu"), Map.entry('め', "me"), Map.entry('も', "mo"),
            Map.entry('や', "ya"), Map.entry('ゆ', "yu"), Map.entry('よ', "yo"),
            Map.entry('ら', "ra"), Map.entry('り', "ri"), Map.entry('る', "ru"), Map.entry('れ', "re"), Map.entry('ろ', "ro"),
            Map.entry('わ', "wa"), Map.entry('を', "wo"), Map.entry('ん', "n"),
            Map.entry('が', "ga"), Map.entry('ぎ', "gi"), Map.entry('ぐ', "gu"), Map.entry('げ', "ge"), Map.entry('ご', "go"),
            Map.entry('ざ', "za"), Map.entry('じ', "ji"), Map.entry('ず', "zu"), Map.entry('ぜ', "ze"), Map.entry('ぞ', "zo"),
            Map.entry('だ', "da"), Map.entry('ぢ', "ji"), Map.entry('づ', "zu"), Map.entry('で', "de"), Map.entry('ど', "do"),
            Map.entry('ば', "ba"), Map.entry('び', "bi"), Map.entry('ぶ', "bu"), Map.entry('べ', "be"), Map.entry('ぼ', "bo"),
            Map.entry('ぱ', "pa"), Map.entry('ぴ', "pi"), Map.entry('ぷ', "pu"), Map.entry('ぺ', "pe"), Map.entry('ぽ', "po"),
            Map.entry('ぁ', "a"), Map.entry('ぃ', "i"), Map.entry('ぅ', "u"), Map.entry('ぇ', "e"), Map.entry('ぉ', "o")
    );

    private final HanyuPinyinOutputFormat pinyinFormat;
    private final Tokenizer japaneseTokenizer;

    public GroundingPhoneticService() {
        this.pinyinFormat = new HanyuPinyinOutputFormat();
        this.pinyinFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        this.pinyinFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        this.pinyinFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        this.japaneseTokenizer = new Tokenizer();
    }

    public Map<String, String> enrich(Map<String, String> labels, Map<String, String> existingPhonetics) {
        Map<String, String> result = new LinkedHashMap<>();
        if (existingPhonetics != null) {
            result.putAll(existingPhonetics);
        }
        String zh = labels == null ? "" : labels.get("zh");
        String ja = labels == null ? "" : labels.get("ja");
        String zhPinyin = chinesePinyin(zh);
        String jaRomaji = japaneseRomaji(ja);
        if (StringUtils.hasText(zhPinyin)) {
            result.put("zh", zhPinyin);
        }
        if (StringUtils.hasText(jaRomaji)) {
            result.put("ja", jaRomaji);
        }
        return result;
    }

    private String chinesePinyin(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        StringBuilder output = new StringBuilder();
        StringBuilder plain = new StringBuilder();
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            String pinyin = pinyinFor(codePoint);
            if (StringUtils.hasText(pinyin)) {
                appendPlain(output, plain);
                appendToken(output, pinyin);
            } else if (Character.isLetterOrDigit(codePoint)) {
                plain.appendCodePoint(codePoint);
            } else {
                appendPlain(output, plain);
            }
        }
        appendPlain(output, plain);
        return output.toString().trim();
    }

    private String pinyinFor(int codePoint) {
        if (codePoint > Character.MAX_VALUE) {
            return "";
        }
        try {
            String[] readings = PinyinHelper.toHanyuPinyinStringArray((char) codePoint, pinyinFormat);
            if (readings == null || readings.length == 0) {
                return "";
            }
            return readings[0];
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            return "";
        }
    }

    private String japaneseRomaji(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        List<Token> tokens = japaneseTokenizer.tokenize(text);
        StringBuilder output = new StringBuilder();
        for (Token token : tokens) {
            String reading = token.getReading();
            if (!StringUtils.hasText(reading) || "*".equals(reading)) {
                reading = token.getSurface();
            }
            String romaji = kanaToRomaji(reading);
            appendToken(output, StringUtils.hasText(romaji) ? romaji : reading.toLowerCase(Locale.ROOT));
        }
        return output.toString().trim();
    }

    private String kanaToRomaji(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        String hiragana = toHiragana(input);
        StringBuilder output = new StringBuilder();
        boolean doubleNext = false;
        for (int index = 0; index < hiragana.length(); index++) {
            char current = hiragana.charAt(index);
            if (current == 'っ') {
                doubleNext = true;
                continue;
            }
            if (current == 'ー') {
                appendLongVowel(output);
                continue;
            }
            String romaji = "";
            if (index + 1 < hiragana.length()) {
                romaji = KANA_DIGRAPHS.getOrDefault(hiragana.substring(index, index + 2), "");
                if (StringUtils.hasText(romaji)) {
                    index++;
                }
            }
            if (!StringUtils.hasText(romaji)) {
                romaji = KANA.getOrDefault(current, Character.toString(current));
            }
            if (doubleNext && startsWithConsonant(romaji)) {
                output.append(romaji.charAt(0));
            }
            doubleNext = false;
            output.append(romaji);
        }
        return output.toString();
    }

    private String toHiragana(String input) {
        StringBuilder output = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char ch = input.charAt(index);
            if (ch >= 'ァ' && ch <= 'ン') {
                output.append((char) (ch - 0x60));
            } else {
                output.append(ch);
            }
        }
        return output.toString();
    }

    private boolean startsWithConsonant(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        char first = value.charAt(0);
        return "aeioun".indexOf(first) < 0;
    }

    private void appendLongVowel(StringBuilder output) {
        for (int index = output.length() - 1; index >= 0; index--) {
            char ch = output.charAt(index);
            if ("aeiou".indexOf(ch) >= 0) {
                output.append(ch);
                return;
            }
        }
    }

    private void appendPlain(StringBuilder output, StringBuilder plain) {
        if (plain.isEmpty()) {
            return;
        }
        appendToken(output, plain.toString().toLowerCase(Locale.ROOT));
        plain.setLength(0);
    }

    private void appendToken(StringBuilder output, String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        if (!output.isEmpty()) {
            output.append(' ');
        }
        output.append(token.trim());
    }
}
