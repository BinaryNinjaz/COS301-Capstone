"use strict";

function kmp_findNext(pattern) {
  var next = [-1];

  var i = 0;
  var j = -1;

  while (i < pattern.length) {
    while (j === -1 || (i < pattern.length && pattern[i] === pattern[j])) {
      i += 1;
      j += 1;
      next.push(j);
    }
    j = next[j];
  }

  return next;
}

function kmp_search(source, pattern) {
  if (source == undefined || pattern == undefined) {
    return -1;
  }

  const next = kmp_findNext(pattern);

  var i = 0;
  var j = 0;

  while (i <= source.length - pattern.length) {
    while (j === -1 || (j < pattern.length && source[i] === pattern[j])) {
      i += 1;
      j += 1;
    }

    if (j == pattern.length) {
      return i - pattern.length;
    }
    j = next[j];
  }

  return -1;
}

function stringContainsWord(string, word) {
  const widx = kmp_search(string, " " + word);
  const sidx = kmp_search(string, word);
  const result = widx !== -1 || sidx === 0;
  return result;
}

function stringContainsSubstring(string, substring) {
  const idx = kmp_search(string, substring);
  const result = idx !== -1;
  return result;
}

function removeAWordFromString(word, string) {
  const widx = kmp_search(string, " " + word);
  const sidx = kmp_search(string, word);
  if (widx !== -1) {
    return string.replace(" " + word, "");
  } else if (sidx === 0) {
    return string.replace(word, "");
  }
  return string;
}

function removeWordFromString(word, string) {
  var result = string;
  while (stringContainsWord(string, word)) {
    const temp = removeAWordFromString(word, result);
    if (temp === result) {
      break;
    }
    result = temp;
  }
  return result;
}
