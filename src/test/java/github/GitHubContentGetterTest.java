package github;

import org.embl.mobie.io.github.GitHubContentGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitHubContentGetterTest {
    private static final String NAME_0 = "default.json";
    public static final String NAME_1 = "manuscript_bookmarks.json";
    private static final String DOWNLOAD_URL_1 = "https://raw.githubusercontent.com/mobie/platybrowser-project/mobie/data/1.0.1/misc/bookmarks/manuscript_bookmarks.json";

    @Test
    public void getContentTest() {
        GitHubContentGetter contentGetter = new GitHubContentGetter("https://github.com/platybrowser/platybrowser",
            "data/1.0.1/misc/bookmarks", "mobie", null);
        String content = contentGetter.getContent();
        Assertions.assertNotNull(content);
        try {
            JSONArray jsonObject = new JSONArray(content);
            Assertions.assertEquals(NAME_0, jsonObject.getJSONObject(0).get("name"));
            Assertions.assertEquals(NAME_1, jsonObject.getJSONObject(1).get("name"));
            Assertions.assertEquals(DOWNLOAD_URL_1, jsonObject.getJSONObject(1).get("download_url"));
        } catch (JSONException err) {
            log.error("Error", err);
        }
    }
}
