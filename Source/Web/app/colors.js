const harvestColorful = [
  'rgba(69, 161, 247, 1)',
  'rgba(127, 211, 81, 1)',
  'rgba(237, 186, 63, 1)',
  'rgba(234, 63, 35, 1)',
  'rgba(178, 79, 128, 1)',
  'rgba(94, 94, 94, 1)'
];

function colorForIndex(index) {
  if (index < 6) {
    return harvestColorful[index];
  } else {
    return randomColor();
  }
}

function randomColor() {
  const hue = Math.round(Math.random() * 360) | 0;
  const sat = Math.round(Math.random() * 50 + 50) | 0;
  const lum = Math.round(Math.random() * 33 + 66) | 0;

  return 'hsla(' + hue + ', ' + sat + '%, ' + lum + '%, 1)';
}

function colorWithAlpha(color, alpha) {
  return color.substr(0, color.length - 3) + alpha + ")";
}

function hashColor(parent, child) {
  const hueRatio = asciiColorHash(parent);
  const satRatio = asciiColorHash(child.substr(0, child.length / 2));
  const briRatio = asciiColorHash(child.substr(child.length / 2, child.length / 2));

  const hue = Math.round(hueRatio * 360) | 0;
  const sat = Math.round(satRatio * 50 + 50) | 0;
  const lum = Math.round(briRatio * 33 + 66) | 0;

  return 'hsla(' + hue + ', ' + sat + '%, ' + lum + '%, 1)';
}

function add32(a, b) {
  return ((a | 0) + (b | 0)) | 0;
}

function mul32(a, b) {
  return ((a | 0) * (b | 0)) | 0;
}

function asciiColorHash(string) {
  var hash = 15487469 | 0;
  for (const i in string) {
    hash = add32(mul32(hash, 1301081), string.charCodeAt(i));
  }
  return Math.abs(hash) / 2147483647.0;
}
