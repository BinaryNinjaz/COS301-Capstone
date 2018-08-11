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

function stringContainsSubstring(string, substring) {
  const idx = kmp_search(string, substring);
  const result = idx !== -1;
  return result;
}
