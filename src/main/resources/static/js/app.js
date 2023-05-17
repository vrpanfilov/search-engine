'use strict';

const sidepanelClassList = document.getElementById('app-sidepanel').classList;

const dashboardClassList = document.getElementById('dashboard-command').classList;
const managementClassList = document.getElementById('management-command').classList;
const searchClassList = document.getElementById('search-command').classList;

function responsiveSidePanel() {
  if (window.innerWidth >= 1200) {
    sidepanelClassList.remove('sidepanel-hidden');
    sidepanelClassList.add('sidepanel-visible');

  } else {
    sidepanelClassList.remove('sidepanel-visible');
    sidepanelClassList.add('sidepanel-hidden');
  }
}

$(function () {

  $(window).on('load', function () {
    console.log('load');
    responsiveSidePanel();
  });

  $(window).resize(function () {
    responsiveSidePanel();
  });

  $('#sidepanel-toggler').click(function () {
    if (sidepanelClassList.contains('sidepanel-visible')) {
      sidepanelClassList.remove('sidepanel-visible');
      sidepanelClassList.add('sidepanel-hidden');
    } else {
      sidepanelClassList.remove('sidepanel-hidden');
      sidepanelClassList.add('sidepanel-visible');
    }
  });

  $('#sidepanel-close').click(function (e) {
    e.preventDefault();
    $('#sidepanel-toggler').click();
  });

  $('#dashboard-command').click(function (e) {
    e.preventDefault();
    $('#up-header').text('Dashboard');
    $('#management').hide();
    $('#search').hide();
    managementClassList.remove('active');
    searchClassList.remove('active');
    $('#dashboard').show();
    dashboardClassList.add('active');
    responsiveSidePanel();
  });

  $('#management-command').click(function (e) {
    e.preventDefault();
    $('#up-header').text('Management');
    $('#dashboard').hide();
    $('#search').hide();
    dashboardClassList.remove('active');
    searchClassList.remove('active');
    $('#management').show();
    managementClassList.add('active');
    responsiveSidePanel();
  });

  $('#search-command').click(function (e) {
    e.preventDefault();
    $('#up-header').text('Search');
    $('#dashboard').hide();
    $('#management').hide();
    dashboardClassList.remove('active');
    managementClassList.remove('active');
    $('#search').show();
    searchClassList.add('active');
    responsiveSidePanel()
  });
});
