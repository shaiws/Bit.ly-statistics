import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Link {
	public String link, title;
	public int clicks;

	public Link(String l, String t) {
		link = l;
		title = t;
		clicks = 0;
	}

	@Override
	public String toString() {
		return "Title: " + title + ", Link: " + link + ", Clicks: " + clicks + "\n";
	}
}

public class JsonReader {

	protected static final String accesToken = "d30800db790c77c71b1d347c0680a5d215115e95";

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	protected static String statistics = "";

	public static void main(String[] args) throws JSONException, IOException {
		Map<Integer, Link> links = new Hashtable<Integer, Link>();
		JFrame f = new JFrame();
		JPanel p = new JPanel();
		JButton b = new JButton("Start");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JSONObject file = null;
				try {
					file = readJsonFromUrl("https://api-ssl.bitly.com/v3/user/link_history?access_token=" + accesToken
							+ "&format=json&limit=100");
				} catch (JSONException | IOException e2) {
					e2.printStackTrace();
				}
				JSONObject data = file.getJSONObject("data");
				JSONArray link_history = data.getJSONArray("link_history");

				for (int i = 0; i < link_history.length(); i++) {
					links.put(i, new Link(link_history.getJSONObject(i).get("link").toString(),
							link_history.getJSONObject(i).get("title").toString()));
				}
				Scanner s;
				for (int j = 0; j < links.size(); j++) {
					String link = links.get(j).link;
					try {
						s = new Scanner(new URL("https://api-ssl.bitly.com/v3/link/clicks?access_token=" + accesToken
								+ "&format=txt&limit=100&link=" + link).openStream());
						int clicks = s.nextInt();
						links.get(j).clicks = clicks;
						statistics += links.get(j).toString();
					} catch (JSONException | IOException e1) {
						e1.printStackTrace();
					}
				}
				try {
					File f = new File((System.getProperty("user.home") + "\\Desktop\\Statics.txt"));
					if (f.exists())
						f.delete();
					f.createNewFile();
					Files.write(Paths.get(f.getAbsolutePath()), statistics.getBytes(), StandardOpenOption.APPEND);
					f.setWritable(false);
				} catch (IOException e1) {

				}
				f.dispose();
			}
		});
		p.add(b);
		f.add(p);
		f.pack();
		f.setVisible(true);
		f.setAlwaysOnTop(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}