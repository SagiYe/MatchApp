package sagiyehezkel.matchappserver;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

public class Util {
	
	public static ArrayList<String> fromJsonArrayToArrayList(JSONArray jsonArray) throws JSONException {
		ArrayList<String> arrayList = new ArrayList<>();

		for (int i=0; i < jsonArray.length(); i++) {
			arrayList.add(jsonArray.getString(i));
		}
		
		return arrayList;
	}

	public static String fromListToString(ArrayList<?> list) {
        StringBuilder sb = new StringBuilder();

        for (Object i : list) {
            sb.append(i);
            sb.append(",");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public static ArrayList<String> fromStringToList(String s) {
        ArrayList<String> list = new ArrayList<String>();

        if (s != null) {
            String[] sArr = s.split(",");
            for (int i = 0; i < sArr.length; ++i) {
                list.add(sArr[i]);
            }
        }

        return list;
    }
}
