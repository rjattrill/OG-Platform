/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.Gadgets_container',
    dependencies: [],
    obj: function () {
        var tabs_tmpl, hdr = ' .ui-layout-header';
        return function (container, gadgets_arr) {
            var selector, initialized = false, live_id, counter = 1,
                gadgets = [], gadgets_container = this;
            /**
             * @param {Number} id Active tab id
             */
            var update_tabs = function (id) {
                var template = Handlebars.compile(tabs_tmpl), $header = $(selector + hdr), tabs_arr;
                if (!id) $header.html(template({'tabs': [{'name': 'empty'}]})); // empty tabs
                else {
                    tabs_arr = gadgets.reduce(function (acc, val) {
                        return acc.push({
                            'name': val.name, 'active': (id === val.id), 'delete': true, 'id': val.id
                        }) && acc;
                    }, []);
                    $header.html(template({'tabs': tabs_arr}));
                    show_gadget(id);
                }
            };
            var show_gadget = function (id) {
                var $gadgets = $(selector).find('.OG-gadget-container [class*=OG-gadget-]');
                $gadgets.filter(function () {return $(this).hasClass('OG-gadget-' + id) ? false : true;}).hide();
                $gadgets.filter('.OG-gadget-' + id).show();
                live_id = id;
            };
            gadgets_container.init = function (container, gadgets_arr) {
                $.when(og.api.text({module: 'og.analytics.tabs_tash'})).then(function (tmpl) {
                    selector = container;
                    tabs_tmpl = tmpl;
                    initialized = true;
                    // init tabs click handler
                    (function () {
                        $(selector + hdr).on('click', 'li', function () {
                            if (!$(this).hasClass('og-active')) {
                                var id = +$(this).attr('class').replace(/og\-/, '');
                                update_tabs(id);
                                gadgets[id-1].gadget.resize();
                            }
                        });
                    }());
                    if (gadgets_arr === void 0) update_tabs();
                    else if ($.isArray(gadgets_arr)) gadgets_container.add(gadgets_arr);
                    else if (gadgets_arr && !$.isArray(gadgets_arr))
                        throw new TypeError('gadgets_arr should be an Array');
                    og.common.gadgets.manager.register(gadgets_container);
                });
            };
            /**
             * @param {Array}          config An array of gadget configuration objects
             * @param {Function}       config[x].gadget
             * @param {Object}         config[x].options
             * @param {String}         config[x].name Tab name
             * @param {Boolean}        config[x].margin Add margin to the container
             */
            gadgets_container.add = function add(config) {
                if (!initialized) return setTimeout(add.partial(config), 10);
                if (!selector) throw new TypeError('Gadgets_container has not been initialized');
                var panel_container = selector + ' .OG-gadget-container';
                config.map(function (obj) {
                    var gadget_class = 'OG-gadget-' + (obj.id = counter++),
                        gadget_selector = panel_container + ' .' + gadget_class;
                    $(panel_container)
                        .append('<div class="' + gadget_class + '" />')
                        .find('.' + gadget_class)
                        .css({margin: obj.margin ? 10 : 0}); // add margin if requested
                    obj.gadget = new obj.gadget($.extend(true, obj.options, {selector: gadget_selector}));
                    gadgets.push(obj);
                });
                update_tabs(config[0].id);
            };
            gadgets_container.alive = function () {return !!$(selector).length};
            gadgets_container.resize = function () {
                gadgets.forEach(function (obj) {if (obj.id === live_id) obj.gadget.resize();});
            }
        };
    }
});