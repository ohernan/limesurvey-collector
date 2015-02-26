package eu.riscoss.datacollectors;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.io.IOUtils;

public class LimeSurveyDataCollector {
	private static final String COLLECTOR_ID = "LimeSurveyCollector";
	private static final String COLLECTOR_DATATYPE = "NUMBER";

	private static JSONObject testInput() {
		JSONObject input = null;
		try {
			input = new JSONObject("{}");
			input.put("username", "riscoss");
			input.put("password", System.getProperty("LimeSurveyDataCollector_PASSWORD"));
			input.put("url", "http://limesurvey.merit.unu.edu");
		    input.put("surveyID","584477");
			input.put("responseID","15");
			input.put("riscoss_targetName", "test");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return input;
	}

    private static double parseValue(String value) {
        if (value.indexOf("A") == 0) { value = value.substring(1); }
        try { return Double.parseDouble(value); } catch (Exception e) { }
        return -1;
    }

	public static void main(String[] args) throws Exception {
		JSONObject input;
		if (args.length > 0 && "--stdin-conf".equals(args[args.length - 1])) {
			String stdin = IOUtils.toString(System.in, "UTF-8");
			input = new JSONObject(stdin);
		} else {
			input = testInput();
			System.out.println("using " + input + " as test configuration.");
			System.out
					.println("In production, use --stdin-conf and pass configuration to stdin");
		}

		LimeSurveyClient limeSurveyClient = new LimeSurveyClient(
				input.getString("username"), input.getString("password"),
				input.getString("url"));

		Hashtable<String, String> questionAnswers = limeSurveyClient
				.getResponse(input.getInt("surveyID"),
						input.getInt("responseID"));

		
		String entity = String.valueOf(questionAnswers.get("ENAME"));
		JSONArray outArray = new JSONArray();
		
		for (String key : questionAnswers.keySet()) {
			if (!key.toString().equals(entity)) {
				JSONObject outObj = new JSONObject();

				outObj.put("id", key.toString());
				outObj.put("type", COLLECTOR_DATATYPE);
				outObj.put("target", entity);
				
				outObj.put("value", parseValue(String.valueOf(questionAnswers.get(key))));
				outArray.put(outObj);
			}
		}
		System.out.println("-----BEGIN RISK DATA-----");
		System.out.println(outArray.toString());
		System.out.println("-----END RISK DATA-----");
	}

}
