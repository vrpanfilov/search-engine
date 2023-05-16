'use strict';

const classList = document.getElementById('app-sidepanel').classList;

function responsiveSidePanel() {
  if (window.innerWidth >= 1200) {
    classList.remove('sidepanel-hidden');
    classList.add('sidepanel-visible');

  } else {
    classList.remove('sidepanel-visible');
    classList.add('sidepanel-hidden');
  }
}

$(function () {
  $(window).on('load', function () {
    responsiveSidePanel();
  });

  $(window).on('resize', function () {
    responsiveSidePanel();
  });

  $('#sidepanel-toggler').on('click', function () {
    if (classList.contains('sidepanel-visible')) {
      classList.remove('sidepanel-visible');
      classList.add('sidepanel-hidden');
    } else {
      classList.remove('sidepanel-hidden');
      classList.add('sidepanel-visible');
    }
  });

  $('#sidepanel-close').on('click', function (e) {
    e.preventDefault();
    $('#sidepanel-toggler').click();
  });

});
