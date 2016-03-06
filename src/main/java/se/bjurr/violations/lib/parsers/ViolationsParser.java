package se.bjurr.violations.lib.parsers;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.quote;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.bjurr.violations.lib.model.Violation;

import com.google.common.base.Optional;

public abstract class ViolationsParser {

 public static Optional<String> findAttribute(String in, String attribute) {
  Pattern pattern = Pattern.compile(attribute + "='([^']+?)'");
  Matcher matcher = pattern.matcher(in);
  if (matcher.find()) {
   return of(matcher.group(1));
  }
  pattern = Pattern.compile(attribute + "=\"([^\"]+?)\"");
  matcher = pattern.matcher(in);
  if (matcher.find()) {
   return of(matcher.group(1));
  }
  return absent();
 }

 public static Integer getIntegerAttribute(String in, String attribute) {
  return parseInt(getAttribute(in, attribute));
 }

 public static String getAttribute(String in, String attribute) {
  Optional<String> foundOpt = findAttribute(in, attribute);
  if (foundOpt.isPresent()) {
   return foundOpt.get();
  }
  throw new RuntimeException("\"" + attribute + "\" not found in \"" + in + "\"");
 }

 public static Optional<Integer> findIntegerAttribute(String in, String attribute) {
  if (findAttribute(in, attribute).isPresent()) {
   return of(parseInt(getAttribute(in, attribute)));
  }
  return absent();
 }

 public static List<String> getChunks(String in, String includingStart, String includingEnd) {
  Pattern pattern = Pattern.compile("(" + includingStart + ".+?" + includingEnd + ")", DOTALL);
  Matcher matcher = pattern.matcher(in);
  List<String> chunks = newArrayList();
  while (matcher.find()) {
   chunks.add(matcher.group());
  }
  return chunks;
 }

 public static String getContent(String in, String tag) {
  Pattern pattern = Pattern.compile("<" + tag + ">[^<]*<!\\[CDATA\\[(" + ".+?" + ")\\]\\]>[^<]*</" + tag + ">", DOTALL);
  Matcher matcher = pattern.matcher(in);
  if (matcher.find()) {
   return matcher.group(1);
  }
  pattern = Pattern.compile("<" + tag + ">(" + ".+?" + ")</" + tag + ">", DOTALL);
  matcher = pattern.matcher(in);
  if (matcher.find()) {
   return matcher.group(1);
  }
  throw new RuntimeException("\"" + tag + "\" not found in " + in);
 }

 public static List<String> getLines(String string) {
  return Arrays.asList(string.split("\n"));
 }

 /**
  * Match one regexp at a time. Remove the matched part from the string, trim,
  * and match next regexp on that string...
  */
 public static List<String> getParts(String string, String... regexpList) {
  List<String> parts = newArrayList();
  for (String regexp : regexpList) {
   Pattern pattern = Pattern.compile(regexp);
   Matcher matcher = pattern.matcher(string);
   boolean found = matcher.find();
   if (!found) {
    return newArrayList();
   }
   String part = matcher.group(1).trim();
   parts.add(part);
   string = string.replaceFirst(quote(matcher.group()), "").trim();
  }
  return parts;
 }

 /**
  * @return List per line in String, with groups from regexpPerLine.
  */
 public static List<List<String>> getLines(String string, String regexpPerLine) {
  List<List<String>> results = newArrayList();
  Pattern pattern = Pattern.compile(regexpPerLine);
  for (String line : string.split("\n")) {
   Matcher matcher = pattern.matcher(line);
   if (!matcher.find()) {
    continue;
   }
   List<String> lineParts = newArrayList();
   for (int g = 0; g <= matcher.groupCount(); g++) {
    lineParts.add(matcher.group(g));
   }
   results.add(lineParts);
  }
  return results;
 }

 public abstract List<Violation> parseFile(File file) throws Exception;

}
