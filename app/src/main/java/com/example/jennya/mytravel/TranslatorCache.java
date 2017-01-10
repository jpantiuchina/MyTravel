package com.example.jennya.mytravel;

import java.util.HashMap;
import java.util.Map;

public class TranslatorCache extends Translator
{
    private final Map<String, String> cache = new HashMap<>();


    public TranslatorCache(String targetLanguage)
    {
        super(targetLanguage);
    }


    @Override
    public String translate(String text)
    {
        String result = cache.get(text);

        if (result != null)
            return result;

        result = super.translate(text);

        cache.put(text, result);

        return result;
    }
}
