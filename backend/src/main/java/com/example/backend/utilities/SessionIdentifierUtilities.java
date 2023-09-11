package com.example.backend.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionIdentifierUtilities {

    public static String generateSessionUniqueIdentifier(String participantOneId, String participantTwoId)
    {
        // sort participant IDs
        String[] idArray = {participantOneId, participantTwoId};
        Arrays.sort(idArray);

        // concat sorted participant IDs
        StringBuffer sb = new StringBuffer();
        for (String str : idArray) {
            sb.append(str);
            sb.append("_");
        }

        return sb.toString();
    }

    public static List<String> getParticipantIDsFromUniqueIdentifier(String sessionUniqueIdentifier)
    {
        List<String> result = new ArrayList<>();

        // Define a regular expression pattern to match two IDs separated by underscores
        Pattern pattern = Pattern.compile("([^_]+)_([^_]+)_");

        // Create a Matcher object to find matches in the input string
        Matcher matcher = pattern.matcher(sessionUniqueIdentifier);

        // Check if the pattern matches the input
        if (matcher.matches()) {
            // Extract the matched groups
            String partIDOne = matcher.group(1);
            String partIDTwo = matcher.group(2);

            // Print the extracted values
            result.add(partIDOne);
            result.add(partIDTwo);
        }

        return result;
    }

    public static String getReceiverFromUniqueIdentifier(String sessionUniqueIdentifier, String senderId){

        List<String> participants = getParticipantIDsFromUniqueIdentifier(sessionUniqueIdentifier);

        for (String participantID : participants){
            if (participantID.equals(senderId)) return participantID;
        }

        return "anonymousUser";
    }

}
