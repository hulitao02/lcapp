layui.define(['jquery','tree'], function (exports) {
    const $ = layui.jquery,
        tree = layui.tree,
        Modules = 'customSelect',
        CustomSelect = function () {
        };

    CustomSelect.prototype.render = function (options) {
        let el = options.el,
            elem = '#' + el,
            $el = $(elem),
            tem = new Date().getTime(),
            list = el + 'SelectDom' + tem,
            listm = '#' + list,
            trigger = options.trigger || 'click',
            datas = options.data,
            showLine = options.line === undefined ? true : options.line,
            accordion = options.accordion === false ? true : options.accordion,
            type = options.type,
            originVal = options.value,
            checked = options.checked,
            checkBoxDiv = `<div id="${list}" class="layui-anim layui-anim-upbit" style="display: none;position: absolute; left: 0; padding: 5px 0; max-height: 350px; overflow-y: scroll; background: #FFFFFF"></div>`

        let checks = [], checkStr = [], checkCode = []
        let handle = {
            checks: [],
            checkStr: [],
            init: function () {
                const _self = this
                $el.val(originVal)
                $el.parent().append(checkBoxDiv)
                function treeForeach (tree) {//遍历树，添加title
                    return tree.forEach(data => {
                        if(!data.title || data.title === undefined) {
                            data.title = data.name || data.text
                            data.children && treeForeach(data.children)
                        }
                    })
                }
                treeForeach(datas)
                tree.render({
                    elem: listm,
                    id: list,
                    data: datas,
                    showCheckbox: type !== 'radio',
                    onlyIconControl: type === 'radio',
                    showLine: showLine,
                    accordion: accordion,
                    click: function (obj) {
                        if (type === 'radio') {
                            $(listm).find('span').css('color', '')
                            $(obj.elem).find('span').css('color', 'red')
                            _self.onclick(obj)
                        }
                    },
                    oncheck: function (obj) {
                        _self.onchecked(obj)
                    }
                })
                $el.on(trigger, function (e) {
                    $(listm).width(e.target.offsetWidth)
                    $(listm).css('top', e.target.offsetTop + e.target.offsetHeight + 1)
                    $(listm).css('left', e.target.offsetLeft)
                    $(listm).show()
                })
                $(document).on('click', function(e){
                    if(e.target.id == el) {
                        $(listm).show()
                    } else {
                        $(listm).hide()
                    }
                })
                $(listm).click(function (e) {
                    e.stopPropagation()
                })
            },
            onclick: function (data) {
                $el.val(data.data.title)
                checked({
                    obj: data,
                    checkedDatas: null,
                    combData: {
                        values: data.data.title,
                        idents: data.data.id,
                        datas: data.data
                    }
                })
                $(listm).hide()
            },
            onchecked: function (obj) {
                let checkedDatas = tree.getChecked(list)
                if(obj.data.children) {
                    if(obj.checked) {
                        checks.push(obj.data)
                        checkCode.push(obj.data.id)
                        checkStr.push(obj.data.title)
                        addchildren(obj.data.children)
                    } else {
                        for(let c = 0;c < checks.length;c ++) {
                            if(obj.data.id == checks[c].id) {
                                checks.splice(c, 1)
                                checkStr.splice(c, 1)
                                checkCode.splice(c, 1)
                            }
                        }
                        removechildren(obj.data.children)
                    }
                } else {
                    if(obj.checked) {
                        checks.push(obj.data)
                        checkCode.push(obj.data.id)
                        checkStr.push(obj.data.title)
                    } else {
                        for(let c = 0;c < checks.length;c ++) {
                            if(obj.data.id == checks[c].id) {
                                checks.splice(c, 1)
                                checkStr.splice(c, 1)
                                checkCode.splice(c, 1)
                            }
                        }
                    }

                }
                $el.val(checkStr.join(','))
                checked({
                    obj,
                    checkedDatas,
                    combData: {
                        values: checkStr,
                        idents: checkCode,
                        datas: checks
                    }
                })
                function addchildren(chid) {
                    for(let i = 0; i < chid.length; i ++) {
                        let chi = chid[i]
                        if(chi.children && chi.children.length > 0) {
                            checks.push(chi)
                            checkStr.push(chi.title)
                            checkCode.push(chi.id)
                            addchildren(chi.children)
                        } else {
                            checks.push(chi)
                            checkStr.push(chi.title)
                            checkCode.push(chi.id)
                        }
                    }
                }
                function removechildren(chid) {
                    for(let i = 0; i < chid.length; i ++) {
                        let chi = chid[i]
                        if(chi.children && chi.children.length > 0) {
                            for(let c = 0;c < checks.length;c ++) {
                                if(chi.id == checks[c].id) {
                                    checks.splice(c, 1)
                                    checkStr.splice(c, 1)
                                    checkCode.splice(c, 1)
                                }
                            }
                            removechildren(chi.children)
                        } else {
                            for(let c = 0;c < checks.length;c ++) {
                                if(chi.id == checks[c].id) {
                                    checks.splice(c, 1)
                                    checkStr.splice(c, 1)
                                    checkCode.splice(c, 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        handle.init()
        return new CustomSelect()
    }
    exports(Modules, new CustomSelect())
})