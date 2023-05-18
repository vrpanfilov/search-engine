'use strict';

$(function () {

  const SidePanel = function () {
    return {
      init: function () {
        function responsiveSidePanel() {
          let sidePanel = $('#app-sidepanel');
          if (window.innerWidth >= 1200) {
            sidePanel.removeClass('sidepanel-hidden');
            sidePanel.addClass('sidepanel-visible');

          } else {
            sidePanel.removeClass('sidepanel-visible');
            sidePanel.addClass('sidepanel-hidden');
          }
        }

        $(window).on('load', function () {
          console.log('load');
          responsiveSidePanel();
        });

        $(window).resize(function () {
          responsiveSidePanel();
        });

        $('#sidepanel-toggler').click(function () {
          let sidePanel = $('#app-sidepanel');
          if (sidePanel.hasClass('sidepanel-visible')) {
            sidePanel.removeClass('sidepanel-visible');
            sidePanel.addClass('sidepanel-hidden');
          } else {
            sidePanel.removeClass('sidepanel-hidden');
            sidePanel.addClass('sidepanel-visible');
          }
        });

        $('#sidepanel-close').click(function (e) {
          e.preventDefault();
          $('#sidepanel-toggler').click();
        });

        $('.nav-command').click(function (e) {
          function switchPage(id, toShow) {
            let name = id.replace("-command", "");
            let div = $('#' + name);
            if (toShow) {
              div.show();
              $('#up-header').text(name.charAt(0).toUpperCase() + name.slice(1));
            } else {
              div.hide();
            }
          }

          e.preventDefault();
          let elem = $(this);
          $('.nav-command').each(function () {
            if ($(this).attr('id') === elem.attr('id')) {
              $(this).addClass('active');
              switchPage($(this).attr('id'), true);
            } else {
              $(this).removeClass('active');
              switchPage($(this).attr('id'), false);
            }
          });
          responsiveSidePanel();
        });
      }
    }
  }
  SidePanel().init();

});
