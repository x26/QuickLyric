/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.geecko.QuickLyric.lyrics;

import com.geecko.QuickLyric.annotations.Reflection;
import com.geecko.QuickLyric.utils.Net;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;

import static com.geecko.QuickLyric.lyrics.Lyrics.ERROR;
import static com.geecko.QuickLyric.lyrics.Lyrics.NEGATIVE_RESULT;
import static com.geecko.QuickLyric.lyrics.Lyrics.POSITIVE_RESULT;

@Reflection
public class LyricsMania {

    @Reflection
    public static final String domain = "www.lyricsmania.com";
    private static final String baseURL = "http://www.lyricsmania.com/%s_lyrics_%s.html";

    @Reflection
    public static Lyrics fromMetaData(String artist, String song) {
        String htmlArtist = Normalizer.normalize(artist.replaceAll("[\\s-]", "_"), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^A-Za-z0-9_]", "");
        String htmlSong = Normalizer.normalize(song.replaceAll("[\\s-]", "_"), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^A-Za-z0-9_]", "");

        String urlString = String.format(
                baseURL,
                htmlSong.toLowerCase(Locale.getDefault()),
                htmlArtist.toLowerCase(Locale.getDefault()));
        return fromURL(urlString, artist, song);
    }

    @Reflection
    public static Lyrics fromURL(String url, String artist, String title) {
        String text;
        try {
            Document document = Jsoup.connect(url).userAgent(Net.USER_AGENT).get();
            Element lyricsBody = document.getElementsByClass("lyrics-body").get(0);
            lyricsBody.select("div").remove().select("strong").remove();
            text = lyricsBody.html();
            if (artist == null)
                artist = document.getElementsByClass("lyrics-nav-menu").get(0)
                        .getElementsByTag("a").get(0).text();
            if (title == null)
                title = document.getElementsByClass("active").text();
        } catch (HttpStatusException e) {
            return new Lyrics(Lyrics.NO_RESULT);
        } catch (IOException e) {
            return new Lyrics(ERROR);
        }
        if (text.startsWith("Instrumental"))
            return new Lyrics(NEGATIVE_RESULT);
        Lyrics lyrics = new Lyrics(POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        lyrics.setURL(url);
        lyrics.setSource(domain);
        lyrics.setText(text);
        return lyrics;
    }
}
