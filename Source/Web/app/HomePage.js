if (/Android/i.test(navigator.userAgent)) {
  if (window.confirm('Harvest has a Native Android App. Tap okay to download the App.')) {
    window.location.href='https://www.google.com/';
  };
} else if (/iPhone|iPad|iPod/i.test(navigator.userAgent)) {
  if (window.confirm('Harvest has a Native iOS App. Tap okay to download the App.')) {
    window.location.href='https://www.apple.com/';
  };
}
