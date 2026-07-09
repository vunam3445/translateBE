package com.example.translateserver.infrastructure.nlp;

import com.example.translateserver.domain.service.SentenceTenseAnalyzer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Triển khai phân tích thì tiếng Anh sử dụng Apache OpenNLP (POS Tagging) + thuật toán so khớp mẫu.
 * Phân tích offline, không cần API ngoài, tốc độ cực nhanh (mili-giây/câu).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenNlpTenseAnalyzer implements SentenceTenseAnalyzer {

    private final OpenNlpModelService modelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Tập hợp từ để nhận diện nhanh
    private static final Set<String> WILL_SHALL = Set.of("will", "shall", "'ll");
    private static final Set<String> HAVE_HAS = Set.of("have", "has");
    private static final Set<String> HAD = Set.of("had");
    private static final Set<String> TO_BE_PRESENT = Set.of("am", "is", "are");
    private static final Set<String> TO_BE_PAST = Set.of("was", "were");
    private static final Set<String> BEEN = Set.of("been");
    private static final Set<String> BEING = Set.of("being");
    private static final Set<String> GOING_TO = Set.of("going");

    /**
     * Thông tin giải thích của 12 thì tiếng Anh bằng tiếng Việt.
     * Mỗi mục gồm: [cấu trúc, dấu hiệu nhận biết]
     */
    private static final class TenseInfo {
        final String nameVi;
        final String structure;
        final String signals;

        TenseInfo(String nameVi, String structure, String signals) {
            this.nameVi = nameVi;
            this.structure = structure;
            this.signals = signals;
        }
    }

    @Override
    public String analyzeSentence(String englishSentence) {
        if (englishSentence == null || englishSentence.isBlank()) {
            return createFallbackJson("Unknown", "Câu đầu vào trống.");
        }

        try {
            log.info("[OpenNlpTenseAnalyzer] Bắt đầu phân tích câu: '{}'", englishSentence);
            String[] tokens = modelService.tokenize(englishSentence);
            log.info("[OpenNlpTenseAnalyzer] Đã tokenize thành {} tokens. Bắt đầu lấy POS tags...", tokens.length);
            String[] tags = modelService.tag(englishSentence);
            log.info("[OpenNlpTenseAnalyzer] Đã lấy xong POS tags: {}", tags != null ? tags.length : "null");

            if (tags == null) {
                log.warn("POS Tagger chưa sẵn sàng, trả về fallback JSON.");
                return createFallbackJson("Unknown", "Chưa khởi tạo được thư viện phân tích ngữ pháp.");
            }

            // Chuẩn hóa tokens về chữ thường để so khớp từ vựng
            String[] tokensLower = Arrays.stream(tokens)
                    .map(String::toLowerCase)
                    .toArray(String[]::new);

            // Phân tích thì từ POS tags và từ vựng
            log.info("[OpenNlpTenseAnalyzer] Bắt đầu chạy detectTense...");
            TenseDetectionResult result = detectTense(tokensLower, tags);
            log.info("[OpenNlpTenseAnalyzer] DetectTense xong. Kết quả thì phát hiện: {}", result.tense);
            TenseInfo info = getTenseInfo(result.tense);

            log.info("[OpenNlpTenseAnalyzer] Bắt đầu buildJson...");
            String finalJson = buildJson(result, info, tokens, tags);
            log.info("[OpenNlpTenseAnalyzer] BuildJson thành công. Độ dài JSON: {}", finalJson.length());
            return finalJson;

        } catch (Exception e) {
            log.error("Lỗi khi phân tích câu: '{}'", englishSentence, e);
            return createFallbackJson("Unknown", "Lỗi khi phân tích: " + e.getMessage());
        }
    }

    /**
     * Thuật toán phát hiện thì dựa trên trình tự trợ động từ và nhãn POS.
     * Sử dụng heuristic theo thứ tự ưu tiên từ phức tạp đến đơn giản.
     */
    private TenseDetectionResult detectTense(String[] tokens, String[] tags) {
        List<String> auxiliaries = new ArrayList<>();
        List<String> mainVerbs = new ArrayList<>();
        boolean hasWillShall = false;
        boolean hasHaveHas = false;
        boolean hasHad = false;
        boolean hasBePres = false;
        boolean hasBePast = false;
        boolean hasBeen = false;
        boolean hasBeing = false;
        boolean hasGoingTo = false;
        String firstModalOrAux = null;

        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i];
            String tag = tags[i];

            if (WILL_SHALL.contains(t) || "MD".equals(tag)) {
                hasWillShall = WILL_SHALL.contains(t);
                if (firstModalOrAux == null) firstModalOrAux = t;
                auxiliaries.add(t);
            } else if (HAVE_HAS.contains(t) && "VBP".equals(tag) || HAVE_HAS.contains(t) && "VBZ".equals(tag) || HAVE_HAS.contains(t) && "VB".equals(tag)) {
                hasHaveHas = true;
                auxiliaries.add(t);
            } else if (HAD.contains(t) && ("VBD".equals(tag) || "VBN".equals(tag) || "VB".equals(tag))) {
                hasHad = true;
                auxiliaries.add(t);
            } else if (TO_BE_PRESENT.contains(t) && ("VBP".equals(tag) || "VBZ".equals(tag) || "VB".equals(tag))) {
                hasBePres = true;
                auxiliaries.add(t);
            } else if (TO_BE_PAST.contains(t) && ("VBD".equals(tag) || "VBN".equals(tag) || "VB".equals(tag))) {
                hasBePast = true;
                auxiliaries.add(t);
            } else if (BEEN.contains(t) && "VBN".equals(tag)) {
                hasBeen = true;
                auxiliaries.add(t);
            } else if (BEING.contains(t) && "VBG".equals(tag)) {
                hasBeing = true;
                auxiliaries.add(t);
            } else if ("to".equals(t) && i > 0 && "going".equals(tokens[i - 1])) {
                hasGoingTo = true;
            } else if (tag.startsWith("VB")) {
                mainVerbs.add(t + "(" + tag + ")");
            }
        }

        // Trích xuất nhãn động từ chính để phân tích
        boolean hasVBG = false; // đang V-ing
        boolean hasVBN = false; // V3 (past participle)
        boolean hasVBD = false; // V2 (past simple)
        boolean hasVBP = false; // hiện tại nguyên mẫu
        boolean hasVBZ = false; // hiện tại ngôi 3 số ít

        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            String t = tokens[i];
            // Bỏ qua các auxiliary đã xét
            boolean isAux = WILL_SHALL.contains(t) || HAVE_HAS.contains(t) || HAD.contains(t)
                    || TO_BE_PRESENT.contains(t) || TO_BE_PAST.contains(t) || BEEN.contains(t) || BEING.contains(t);
            if (isAux) continue;

            if ("VBG".equals(tag)) hasVBG = true;
            else if ("VBN".equals(tag)) hasVBN = true;
            else if ("VBD".equals(tag)) hasVBD = true;
            else if ("VBP".equals(tag)) hasVBP = true;
            else if ("VBZ".equals(tag)) hasVBZ = true;
        }

        String tense;
        String explanation;
        String signalWords = String.join(", ", auxiliaries);

        // ===== Xác định thì theo thứ tự ưu tiên từ phức tạp đến đơn giản =====

        if (hasWillShall && hasBeen && hasVBG) {
            // will + have been + V-ing -> Future Perfect Continuous
            tense = "Future Perfect Continuous";
            explanation = "Câu chứa trợ động từ '" + signalWords + "' kết hợp với 'been' và động từ ở dạng V-ing, tạo thành cấu trúc thì Tương lai hoàn thành tiếp diễn.";
        } else if (hasWillShall && hasHaveHas && hasVBN) {
            // will + have + V3 -> Future Perfect
            tense = "Future Perfect";
            explanation = "Câu chứa trợ động từ tương lai '" + signalWords + "' kết hợp với 'have' và động từ ở dạng phân từ V3/V-ed.";
        } else if (hasWillShall && hasVBG) {
            // will + be + V-ing -> Future Continuous
            tense = "Future Continuous";
            explanation = "Câu chứa trợ động từ tương lai '" + signalWords + "' kết hợp với 'be' và động từ ở dạng V-ing.";
        } else if (hasWillShall || (hasGoingTo && hasBePres)) {
            // will + V hoặc am/is/are + going to + V -> Future Simple
            tense = "Future Simple";
            if (hasGoingTo && hasBePres) {
                explanation = "Câu sử dụng cấu trúc 'am/is/are going to' để diễn đạt kế hoạch hoặc dự định trong tương lai.";
            } else {
                explanation = "Câu chứa trợ động từ tương lai '" + signalWords + "' (will/shall) đứng trước động từ nguyên mẫu.";
            }
        } else if (hasHad && hasBeen && hasVBG) {
            // had + been + V-ing -> Past Perfect Continuous
            tense = "Past Perfect Continuous";
            explanation = "Câu chứa trợ động từ 'had' kết hợp với 'been' và động từ V-ing, tạo thành thì Quá khứ hoàn thành tiếp diễn.";
        } else if (hasHad && hasVBN) {
            // had + V3 -> Past Perfect
            tense = "Past Perfect";
            explanation = "Câu chứa trợ động từ 'had' kết hợp với động từ ở dạng phân từ V3/V-ed, đặc trưng của thì Quá khứ hoàn thành.";
        } else if (hasBePast && hasVBG) {
            // was/were + V-ing -> Past Continuous
            tense = "Past Continuous";
            explanation = "Câu chứa động từ tobe quá khứ '" + signalWords + "' kết hợp với động từ V-ing.";
        } else if (hasHaveHas && hasBeen && hasVBG) {
            // have/has + been + V-ing -> Present Perfect Continuous
            tense = "Present Perfect Continuous";
            explanation = "Câu chứa trợ động từ '" + signalWords + "' kết hợp với 'been' và động từ V-ing, tạo thành thì Hiện tại hoàn thành tiếp diễn.";
        } else if (hasHaveHas && hasVBN) {
            // have/has + V3 -> Present Perfect
            tense = "Present Perfect";
            explanation = "Câu chứa trợ động từ '" + signalWords + "' kết hợp với động từ ở dạng phân từ V3/V-ed, đặc trưng của thì Hiện tại hoàn thành.";
        } else if (hasBePres && hasVBG) {
            // am/is/are + V-ing -> Present Continuous
            tense = "Present Continuous";
            explanation = "Câu chứa động từ tobe hiện tại '" + signalWords + "' kết hợp với động từ V-ing.";
        } else if (hasBePast && hasVBN) {
            // was/were + V3 -> Past Simple Passive (nhưng xếp vào Past Simple)
            tense = "Past Simple";
            explanation = "Câu sử dụng động từ tobe quá khứ '" + signalWords + "' kết hợp với V3 (thể bị động của thì Quá khứ đơn).";
        } else if (hasVBD) {
            // Động từ chia ở quá khứ đơn -> Past Simple
            tense = "Past Simple";
            explanation = "Câu chứa động từ chia ở dạng quá khứ đơn (V2/V-ed), không có trợ động từ đặc biệt kèm theo.";
        } else if (hasBePres) {
            // am/is/are + Noun/Adj -> Present Simple (dạng tobe)
            tense = "Present Simple";
            explanation = "Câu sử dụng động từ tobe hiện tại '" + signalWords + "' để diễn tả trạng thái hoặc mô tả đặc điểm.";
        } else if (hasVBP || hasVBZ) {
            // Động từ chia ở hiện tại đơn -> Present Simple
            tense = "Present Simple";
            explanation = "Câu chứa động từ chia ở dạng hiện tại đơn (V-inf hoặc V-s/es), không có trợ động từ tiếp diễn hoặc hoàn thành.";
        } else {
            tense = "Present Simple";
            explanation = "Không xác định được rõ ràng cấu trúc động từ. Phân tích mặc định là Hiện tại đơn.";
        }

        return new TenseDetectionResult(tense, explanation, auxiliaries);
    }

    private TenseInfo getTenseInfo(String tense) {
        return switch (tense) {
            case "Present Simple" -> new TenseInfo(
                    "Thì Hiện tại đơn",
                    "S + V-inf / V-s/es | S + am/is/are + N/Adj | S + do/does + V-inf",
                    "Diễn đạt thói quen, sự thật hiển nhiên, lịch trình cố định. Dấu hiệu: always, usually, often, sometimes, never, every day/week..."
            );
            case "Present Continuous" -> new TenseInfo(
                    "Thì Hiện tại tiếp diễn",
                    "S + am/is/are + V-ing",
                    "Diễn đạt hành động đang xảy ra tại thời điểm nói. Dấu hiệu: now, right now, at the moment, at present, look!, listen!"
            );
            case "Present Perfect" -> new TenseInfo(
                    "Thì Hiện tại hoàn thành",
                    "S + have/has + V3/V-ed",
                    "Diễn đạt hành động đã hoàn thành nhưng có liên quan đến hiện tại. Dấu hiệu: since, for, already, yet, just, ever, never, recently..."
            );
            case "Present Perfect Continuous" -> new TenseInfo(
                    "Thì Hiện tại hoàn thành tiếp diễn",
                    "S + have/has + been + V-ing",
                    "Diễn đạt hành động đã bắt đầu trong quá khứ và vẫn đang tiếp tục. Dấu hiệu: since, for, how long, all day/morning..."
            );
            case "Past Simple" -> new TenseInfo(
                    "Thì Quá khứ đơn",
                    "S + V2/V-ed | S + was/were + N/Adj | S + did + V-inf",
                    "Diễn đạt hành động đã hoàn toàn kết thúc trong quá khứ. Dấu hiệu: yesterday, ago, last (week/month/year), in + năm quá khứ..."
            );
            case "Past Continuous" -> new TenseInfo(
                    "Thì Quá khứ tiếp diễn",
                    "S + was/were + V-ing",
                    "Diễn đạt hành động đang diễn ra tại một thời điểm xác định trong quá khứ. Dấu hiệu: at + giờ + yesterday, while, when, at that time..."
            );
            case "Past Perfect" -> new TenseInfo(
                    "Thì Quá khứ hoàn thành",
                    "S + had + V3/V-ed",
                    "Diễn đạt hành động xảy ra và hoàn thành trước một hành động khác trong quá khứ. Dấu hiệu: before, after, by the time, already, just, when (dùng với Past Simple)..."
            );
            case "Past Perfect Continuous" -> new TenseInfo(
                    "Thì Quá khứ hoàn thành tiếp diễn",
                    "S + had + been + V-ing",
                    "Diễn đạt hành động đã đang xảy ra liên tục trước một thời điểm/hành động khác trong quá khứ. Dấu hiệu: for, since, before, when, by the time..."
            );
            case "Future Simple" -> new TenseInfo(
                    "Thì Tương lai đơn",
                    "S + will/shall + V-inf | S + am/is/are + going to + V-inf",
                    "Diễn đạt quyết định tức thời, dự đoán, lời hứa, hoặc kế hoạch tương lai. Dấu hiệu: tomorrow, next (week/month/year), soon, in the future, tonight..."
            );
            case "Future Continuous" -> new TenseInfo(
                    "Thì Tương lai tiếp diễn",
                    "S + will/shall + be + V-ing",
                    "Diễn đạt hành động sẽ đang xảy ra tại một thời điểm xác định trong tương lai. Dấu hiệu: at this time tomorrow, at + giờ + tomorrow, this time next week..."
            );
            case "Future Perfect" -> new TenseInfo(
                    "Thì Tương lai hoàn thành",
                    "S + will/shall + have + V3/V-ed",
                    "Diễn đạt hành động sẽ hoàn thành trước một thời điểm/hành động khác trong tương lai. Dấu hiệu: by (the time), before, by + thời gian tương lai..."
            );
            case "Future Perfect Continuous" -> new TenseInfo(
                    "Thì Tương lai hoàn thành tiếp diễn",
                    "S + will/shall + have + been + V-ing",
                    "Diễn đạt hành động sẽ đang xảy ra liên tục tính đến một thời điểm xác định trong tương lai. Dấu hiệu: for + khoảng thời gian, by the time + thời gian tương lai..."
            );
            default -> new TenseInfo(
                    "Không xác định",
                    "Không xác định",
                    "Không xác định được thì của câu."
            );
        };
    }

    private String buildJson(TenseDetectionResult result, TenseInfo info,
                              String[] tokens, String[] tags) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("tense", result.tense);
            root.put("tense_vi", info.nameVi);
            root.put("tense_structure", info.structure);
            root.put("tense_signals", info.signals);
            root.put("tense_explanation", result.explanation);

            // Danh sách token kèm POS tag
            ArrayNode wordsArray = root.putArray("words");
            for (int i = 0; i < tokens.length; i++) {
                ObjectNode wordNode = wordsArray.addObject();
                wordNode.put("w", tokens[i]);
                wordNode.put("p", i < tags.length ? tags[i] : "?");
                wordNode.put("p_vi", translatePosTag(i < tags.length ? tags[i] : "?"));
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.error("Lỗi khi build JSON kết quả phân tích", e);
            return createFallbackJson(result.tense, result.explanation);
        }
    }

    /**
     * Dịch nhãn Penn Treebank POS Tag sang mô tả tiếng Việt cho người dùng cuối.
     */
    private String translatePosTag(String tag) {
        return switch (tag) {
            case "NN", "NNS" -> "Danh từ";
            case "NNP", "NNPS" -> "Danh từ riêng";
            case "VB" -> "Động từ nguyên mẫu";
            case "VBD" -> "Động từ quá khứ (V2)";
            case "VBG" -> "Động từ V-ing";
            case "VBN" -> "Động từ phân từ V3";
            case "VBP" -> "Động từ hiện tại";
            case "VBZ" -> "Động từ hiện tại (ngôi 3 số ít)";
            case "MD" -> "Động từ khiếm khuyết (Modal)";
            case "JJ", "JJR", "JJS" -> "Tính từ";
            case "RB", "RBR", "RBS" -> "Trạng từ";
            case "IN" -> "Giới từ";
            case "DT" -> "Mạo từ / Từ hạn định";
            case "PRP" -> "Đại từ nhân xưng";
            case "PRP$" -> "Đại từ sở hữu";
            case "CC" -> "Liên từ";
            case "CD" -> "Số đếm";
            case "TO" -> "Từ 'to'";
            case "WP", "WDT", "WRB" -> "Từ để hỏi";
            case "EX" -> "Từ 'there' (tồn tại)";
            case ".", ",", ":", "\"", "(", ")" -> "Dấu câu";
            default -> tag;
        };
    }

    private String createFallbackJson(String tense, String reason) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("tense", tense);
            root.put("tense_vi", "Không xác định");
            root.put("tense_structure", "Không xác định");
            root.put("tense_signals", "Không xác định");
            root.put("tense_explanation", reason);
            root.putArray("words");
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"tense\":\"Unknown\",\"tense_explanation\":\"Lỗi hệ thống\"}";
        }
    }

    /**
     * Data class chứa kết quả phát hiện thì.
     */
    private static class TenseDetectionResult {
        final String tense;
        final String explanation;
        final List<String> auxiliaries;

        TenseDetectionResult(String tense, String explanation, List<String> auxiliaries) {
            this.tense = tense;
            this.explanation = explanation;
            this.auxiliaries = auxiliaries;
        }
    }
}
