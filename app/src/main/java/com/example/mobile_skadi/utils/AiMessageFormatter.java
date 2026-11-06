package com.example.mobile_skadi.utils;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiMessageFormatter {

    /**
     * Limpa respostas brutas e formata em texto elegante com marcadores e quebras de linha.
     */
    public static CharSequence format(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "";
        }

        String cleaned = rawText.trim();

        // 1. Remove milissegundos das datas (ex: 2026-11-06 12:09:07.416833 -> 2026-11-06 12:09:07)
        cleaned = cleaned.replaceAll("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\.\\d+", "$1");

        // 2. Substitui enums brutos por termos legíveis
        cleaned = cleaned.replaceAll("(?i)\\bTEMPERATURA_FORA_PADRAO\\b", "Temperatura Fora do Padrão");
        cleaned = cleaned.replaceAll("(?i)\\bDESVIO_CRITICO\\b", "Desvio Crítico");
        cleaned = cleaned.replaceAll("(?i)\\bCRITICO\\b", "Crítico");
        cleaned = cleaned.replaceAll("(?i)\\bALTO\\b", "Alto");
        cleaned = cleaned.replaceAll("(?i)\\bMEDIO\\b", "Médio");
        cleaned = cleaned.replaceAll("(?i)\\bBAIXO\\b", "Baixo");
        cleaned = cleaned.replaceAll("(?i)\\bRECONHECIDO\\b", "Reconhecido");
        cleaned = cleaned.replaceAll("(?i)\\bATIVO\\b", "Ativo");
        cleaned = cleaned.replaceAll("(?i)\\bRESOLVIDO\\b", "Resolvido");

        // 3. Quebra itens concatenados de Câmaras ou Setores em tópicos com marcadores
        // Exemplo: ". Câmara gestor" -> ".\n\n• Câmara gestor"
        cleaned = cleaned.replaceAll("(?<=\\.|;)\\s+(?=(?:Câmara|Camara|Setor|Unidade)\\b)", "\n\n• ");

        // Se começar com Câmara ou Setor e não tiver bullet:
        if (cleaned.startsWith("Câmara ") || cleaned.startsWith("Camara ") || cleaned.startsWith("Setor ")) {
            cleaned = "• " + cleaned;
        }

        // 4. Se houver listas numeradas como "1. " ou "1 - ", garante espaçamento
        cleaned = cleaned.replaceAll("(?<=\\n|^)(\\d+)[\\.\\-]\\s*", "$1. ");

        // 5. Suporte a Markdown básico: formata **texto em negrito**
        return applyMarkdownBold(cleaned);
    }

    private static CharSequence applyMarkdownBold(String text) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
        Matcher matcher = pattern.matcher(text);

        int lastIndex = 0;
        while (matcher.find()) {
            ssb.append(text.substring(lastIndex, matcher.start()));
            String boldText = matcher.group(1);
            int startSpan = ssb.length();
            ssb.append(boldText);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), startSpan, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            lastIndex = matcher.end();
        }
        if (lastIndex < text.length()) {
            ssb.append(text.substring(lastIndex));
        }

        return ssb;
    }
}
