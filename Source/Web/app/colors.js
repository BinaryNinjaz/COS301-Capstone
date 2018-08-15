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
  const sat = Math.round(Math.random() * 25 + 75) | 0;
  const lum = Math.round(Math.random() * 25 + 75) | 0;

  return 'hsla(' + hue + ', ' + sat + '%, ' + lum + '%, 1)';
}

function colorWithAlpha(color, alpha) {
  return color.substr(0, color.length - 3) + alpha + ")";
}
