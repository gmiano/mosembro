package com.lexandera.mosembro.jsinterfaces;

import java.util.HashMap;

import com.lexandera.mosembro.Mosembro;

/** 
 * Saves configuration params for site-level search
 * It is used by /res/raw/search_form.js 
 */
public class SiteSearchInterface
{
    private Mosembro browser;
  
    public SiteSearchInterface(Mosembro browser)
    {
        this.browser = browser;
    }
    
    public void setSearchFormData(String formAction, String inputName, String description)
    {
        String searchDescription = "Search site";
        if (description.length() > 1) {
            searchDescription = description;
        }
        
        HashMap<String, String> siteSearchConfig = new HashMap<String, String>();
        siteSearchConfig.put("formAction", formAction);
        siteSearchConfig.put("inputName", inputName);
        siteSearchConfig.put("searchDescription", searchDescription);
        browser.setSiteSearchOptions(true, siteSearchConfig);
    }
    
}
