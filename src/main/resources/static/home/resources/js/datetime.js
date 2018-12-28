//date
(function($) {
  $.fn.mdater = function(config) {
    var defaults = {
      maxDate: null,
      minDate: new Date(1970, 0, 1)
    };
    var option = $.extend(defaults, config);
    //window.console && console.log(this);
    var input = this;

    //通用函数
    var F = {
      //计算某年某月有多少天
      getDaysInMonth: function(year, month) {
        return new Date(year, month + 1, 0).getDate();
      },
      //计算某月1号是星期几
      getWeekInMonth: function(year, month) {
        return new Date(year, month, 1).getDay();
      },
      getMonth: function(m) {
        return ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '十一', '十二'][m];
      },
      //计算年某月的最后一天日期
      getLastDayInMonth: function(year, month) {
        return new Date(year, month, this.getDaysInMonth(year, month));
      }
    }

    //为$扩展一个方法，以配置的方式代理事件
    $.fn.delegates = function(configs) {
      el = $(this[0]);
      for (var name in configs) {
        var value = configs[name];
        if (typeof value == 'function') {
          var obj = {};
          obj.click = value;
          value = obj;
        };
        for (var type in value) {
          el.delegate(name, type, value[type]);
        }
      }
      return this;
    }

    var mdater = {
      value: {
        year: '',
        month: '',
        date: ''
      },
      lastCheckedDate: '',
      init: function() {
        this.initListeners();
      },
      renderHTML: function() {
        var $html = $('<div class="md_mask"></div><div class="md_panel"><div class="md_head"><div class="md_selectarea"><a class="md_prev change_year" href="javascript:void(0);"><</a><a class="md_headtext yeartag" href="javascript:void(0);"></a><a class="md_next change_year" href="javascript:void(0);">></a></div><div class="md_selectarea"><a class="md_prev change_month" href="javascript:void(0);"><</a><a class="md_headtext monthtag" href="javascript:void(0);">月</a> <a class="md_next change_month" href="javascript:void(0);">></a></div></div><div class="md_body"><ul class="md_weekarea"><li>日</li><li>一</li><li>二</li><li>三</li><li>四</li><li>五</li><li>六</li></ul><ul class="md_datearea in"></ul></div><div class="md_foot"><a href="javascript:void(0);" class="md_ok">确定</a> <a href="javascript:void(0);" class="md_cancel">取消</a></div></div>');
        if ($('.md_mask').length == 0) { $(document.body).append($html); }
        return $html;
      },
      _showPanel: function(container) {
        this.refreshView();
        $('.md_panel, .md_mask').addClass('show');
      },
      _hidePanel: function() {
        //$('.md_panel, .md_mask').removeClass('show');
        $('.md_panel, .md_mask').remove();
      },
      _changeMonth: function(add, checkDate) {

        //先把已选择的日期保存下来
        this.saveCheckedDate();

        var monthTag = $('.md_selectarea').find('.monthtag'),
          num = ~~monthTag.data('month') + add;
        //月份变动发生了跨年
        if (num > 11) {
          num = 0;
          this.value.year++;
          $('.yeartag').text(this.value.year).data('year', this.value.year);
        } else if (num < 0) {
          num = 11;
          this.value.year--;
          $('.yeartag').text(this.value.year).data('year', this.value.year);
        }

        var nextMonth = F.getMonth(num) + '月';
        monthTag.text(nextMonth).data('month', num);
        this.value.month = num;
        if (checkDate) {
          this.value.date = checkDate;
        } else {
          //如果有上次选择的数据，则进行赋值
          this.setCheckedDate();
        }
        this.updateDate(add);
      },
      _changeYear: function(add) {
        //先把已选择的日期保存下来
        this.saveCheckedDate();

        var yearTag = $('.md_selectarea').find('.yeartag'),
          num = ~~yearTag.data('year') + add;
        yearTag.text(num + '年').data('year', num);
        this.value.year = num;

        this.setCheckedDate();

        this.updateDate(add);
      },
      //保存上一次选择的数据
      saveCheckedDate: function() {
        if (this.value.date) {
          this.lastCheckedDate = {
            year: this.value.year,
            month: this.value.month,
            date: this.value.date
          }
        }
      },
      //将上一次保存的数据恢复到界面
      setCheckedDate: function() {
        if (this.lastCheckedDate && this.lastCheckedDate.year == this.value.year && this.lastCheckedDate.month == this.value.month) {
          this.value.date = this.lastCheckedDate.date;
        } else {
          this.value.date = '';
        }
      },
      //根据日期得到渲染天数的显示的HTML字符串
      getDateStr: function(y, m, d) {
        var dayStr = '';
        //计算1号是星期几，并补上上个月的末尾几天
        var week = F.getWeekInMonth(y, m);
        var lastMonthDays = F.getDaysInMonth(y, m - 1);
        for (var j = week - 1; j >= 0; j--) {
          dayStr += '<li class="prevdate" data-day="' + (lastMonthDays - j) + '">' + (lastMonthDays - j) + '</li>';
        }
        //再补上本月的所有天;
        var currentMonthDays = F.getDaysInMonth(y, m);
        //判断是否超出允许的日期范围
        var startDay = 1,
          endDay = currentMonthDays,
          thisDate = new Date(y, m, d),
          firstDate = new Date(y, m, 1);
        lastDate = new Date(y, m, currentMonthDays),
          minDateDay = option.minDate.getDate();


        if (option.minDate > lastDate) {
          startDay = currentMonthDays + 1;
        } else if (option.minDate >= firstDate && option.minDate <= lastDate) {
          startDay = minDateDay;
        }

        if (option.maxDate) {
          var maxDateDay = option.maxDate.getDate();
          if (option.maxDate < firstDate) {
            endDay = startDay - 1;
          } else if (option.maxDate >= firstDate && option.maxDate <= lastDate) {
            endDay = maxDateDay;
          }
        }


        //将日期按允许的范围分三段拼接
        for (var i = 1; i < startDay; i++) {
          dayStr += '<li class="disabled" data-day="' + i + '">' + i + '</li>';
        }
        for (var j = startDay; j <= endDay; j++) {
          var current = '';
          if (y == this.value.year && m == this.value.month && d == j) {
            current = 'current';
          }
          dayStr += '<li class="' + current + '" data-day="' + j + '">' + j + '</li>';
        }
        for (var k = endDay + 1; k <= currentMonthDays; k++) {
          dayStr += '<li class="disabled" data-day="' + k + '">' + k + '</li>';
        }

        //再补上下个月的开始几天
        var nextMonthStartWeek = (currentMonthDays + week) % 7;
        if (nextMonthStartWeek !== 0) {
          for (var i = 1; i <= 7 - nextMonthStartWeek; i++) {
            dayStr += '<li class="nextdate" data-day="' + i + '">' + i + '</li>';
          }
        }

        return dayStr;
      },
      updateDate: function(add) {
        var dateArea = $('.md_datearea.in');
        if (add == 1) {
          var c1 = 'out_left';
          var c2 = 'out_right';
        } else {
          var c1 = 'out_right';
          var c2 = 'out_left';
        }
        var newDateArea = $('<ul class="md_datearea ' + c2 + '"></ul>');
        newDateArea.html(this.getDateStr(this.value.year, this.value.month, this.value.date));
        $('.md_body').append(newDateArea);
        setTimeout(function() {
          newDateArea.removeClass(c2).addClass('in');
          dateArea.removeClass('in').addClass(c1);
        }, 0);

      },
      //每次调出panel前，对界面进行重置
      refreshView: function() {
        if (this.input.hasClass('input-group')) {
          var initVal = this.input.children('input').val();
        } else {
          var initVal = this.input.val();
        }
        var date = null;
        if (initVal) {
          var arr = initVal.split('-');
          date = new Date(arr[0], arr[1] - 1, arr[2]);
        } else {
          date = new Date();
        }
        var y = this.value.year = date.getFullYear(),
          m = this.value.month = date.getMonth(),
          d = this.value.date = date.getDate();
        $('.yeartag').text(y).data('year', y);
        $('.monthtag').text(F.getMonth(m) + '月').data('month', m);
        var dayStr = this.getDateStr(y, m, d);
        $('.md_datearea').html(dayStr);
      },
      input: null, //暂存当前指向input
      initListeners: function() {
        var _this = this;
        input.on('click', function() {
          _this.input = $(this); //暂存当前指向input
          if ($('.md_mask').length) {
            _this._hidePanel();
          } else {
            _this.renderHTML();
            var panel = $('.md_panel'),
              mask = $('.md_mask');
            _this.afterShowPanel(mask, panel);
            setTimeout(function() {
              _this._showPanel();
            }, 50);
          }
        });
      },
      saveValueToInput: function() {
        var _this = this;
        var monthValue = ~~_this.value.month + 1;
        if (monthValue < 10) {
          monthValue = '0' + monthValue;
        }
        var dateValue = _this.value.date;
        if (dateValue === '') {
          dateValue = _this.value.date = 1;
        }
        if (dateValue < 10) {
          dateValue = '0' + dateValue;
        }
        if (_this.input.hasClass('input-group')) {
          _this.input.children('input').val(_this.value.year + '-' + monthValue + '-' + dateValue);
          _this.input.children('input').trigger('input');
        } else {
          _this.input.val(_this.value.year + '-' + monthValue + '-' + dateValue);
          _this.input.trigger('input');
        }
        _this._hidePanel();
      },
      afterShowPanel: function(mask, panel) {
        var _this = this;
        mask.on('click', function() {
          _this._hidePanel();
        });
        panel.delegates({
          '.change_month': function() {
            var add = $(this).hasClass('md_next') ? 1 : -1;
            _this._changeMonth(add);
          },
          '.change_year': function() {
            var add = $(this).hasClass('md_next') ? 1 : -1;
            _this._changeYear(add);
          },
          '.out_left, .out_right': {
            'webkitTransitionEnd': function() {
              $(this).remove();
            }
          },
          '.md_datearea li': function() {
            var $this = $(this);
            if ($this.hasClass('disabled')) {
              return;
            }
            _this.value.date = $this.data('day');
            //判断是否点击的是前一月或后一月的日期
            var add = 0;
            if ($this.hasClass('nextdate')) {
              add = 1;
            } else if ($this.hasClass('prevdate')) {
              add = -1;
            }
            if (add !== 0) {
              _this._changeMonth(add, _this.value.date);
            } else {
              $this.addClass('current').siblings('.current').removeClass('current');
              _this.saveValueToInput();
            }
          },
          '.md_cancel': function() {
            _this._hidePanel();
          },
          '.md_ok': function() {
            _this.saveValueToInput();
          }
        });
      }
    }
    mdater.init();
  }
})(window.Zepto || window.jQuery);

//time
/*!
 * mobileSelect.js
 * (c) 2017-present onlyhom
 * Released under the MIT License.
 */

(function() {
	function getClass(dom,string) {
		return dom.getElementsByClassName(string);
	}
	//构造器
	function MobileSelect(config) {
		this.mobileSelect;
		this.wheelsData = config.wheels;
		this.jsonType =  false;
		this.cascadeJsonData = [];
		this.displayJson = [];
		this.curValue = null;
		this.curIndexArr = [];
		this.cascade = false;
		this.startY;
		this.moveEndY;
		this.moveY;
		this.oldMoveY;
		this.offset = 0;
		this.offsetSum = 0;
		this.oversizeBorder;
		this.curDistance = [];
		this.clickStatus = false;
		this.isPC = true;
		this.init(config);
	}
	MobileSelect.prototype = {
		constructor: MobileSelect,
		init: function(config){
			var _this = this;
			_this.keyMap = config.keyMap ? config.keyMap : {id:'id', value:'value', childs:'childs'};
			_this.checkDataType();
			_this.renderWheels(_this.wheelsData, config.cancelBtnText, config.ensureBtnText);
			_this.trigger = document.querySelector(config.trigger);
			if(!_this.trigger){
				console.error('mobileSelect has been successfully installed, but no trigger found on your page.');
				return false;
			}
			_this.wheel = getClass(_this.mobileSelect,'wheel');
			_this.slider = getClass(_this.mobileSelect,'selectContainer');
			_this.wheels = _this.mobileSelect.querySelector('.wheels');
			_this.liHeight = _this.mobileSelect.querySelector('li').offsetHeight;
			_this.ensureBtn = _this.mobileSelect.querySelector('.ensure');
			_this.cancelBtn = _this.mobileSelect.querySelector('.cancel');
			_this.grayLayer = _this.mobileSelect.querySelector('.grayLayer');
			_this.popUp = _this.mobileSelect.querySelector('.content');
			_this.callback = config.callback || function(){};
			_this.transitionEnd = config.transitionEnd || function(){};
			_this.onShow = config.onShow || function(){};
			_this.onHide = config.onHide || function(){};
			_this.initPosition = config.position || [];
			_this.titleText = config.title || '';
			_this.connector = config.connector || ' ';
			_this.triggerDisplayData = !(typeof(config.triggerDisplayData)=='undefined') ? config.triggerDisplayData : true;
			_this.trigger.style.cursor='pointer';
			_this.setStyle(config);
			_this.setTitle(_this.titleText);
			_this.checkIsPC();
			_this.checkCascade();
			_this.addListenerAll();

			if (_this.cascade) {
				_this.initCascade();
			}
			//定位 初始位置
			if(_this.initPosition.length < _this.slider.length){
				var diff = _this.slider.length - _this.initPosition.length;
				for(var i=0; i<diff; i++){
					_this.initPosition.push(0);
				}
			}

			_this.setCurDistance(_this.initPosition);


			//按钮监听
			_this.cancelBtn.addEventListener('click',function(){
				_this.hide();
		    });

		    _this.ensureBtn.addEventListener('click',function(){
				_this.hide();
			    if(!_this.liHeight) {
			        _this.liHeight =  _this.mobileSelect.querySelector('li').offsetHeight;
			    }
				var tempValue ='';
		    	for(var i=0; i<_this.wheel.length; i++){
		    		i==_this.wheel.length-1 ? tempValue += _this.getInnerHtml(i) : tempValue += _this.getInnerHtml(i) + _this.connector;
		    	}
		    	if(_this.triggerDisplayData){
		    		_this.trigger.innerHTML = tempValue;
		    	}
		    	_this.curIndexArr = _this.getIndexArr();
		    	_this.curValue = _this.getCurValue();
		    	_this.callback(_this.curIndexArr, _this.curValue);
		    });

		    _this.trigger.addEventListener('click',function(){
		    	_this.show();
		    });
		    _this.grayLayer.addEventListener('click',function(){
				_this.hide();
		    });
		    _this.popUp.addEventListener('click',function(){
		    	event.stopPropagation();
		    });

			_this.fixRowStyle(); //修正列数
		},

		setTitle: function(string){
			var _this = this;
			_this.titleText = string;
			_this.mobileSelect.querySelector('.title').innerHTML = _this.titleText;
		},

		setStyle: function(config){
			var _this = this;
			if(config.ensureBtnColor){
				_this.ensureBtn.style.color = config.ensureBtnColor;
			}
			if(config.cancelBtnColor){
				_this.cancelBtn.style.color = config.cancelBtnColor;
			}
			if(config.titleColor){
				_this.title = _this.mobileSelect.querySelector('.title');
				_this.title.style.color = config.titleColor;
			}
			if(config.textColor){
				_this.panel = _this.mobileSelect.querySelector('.panel');
				_this.panel.style.color = config.textColor;
			}
			if(config.titleBgColor){
				_this.btnBar = _this.mobileSelect.querySelector('.btnBar');
				_this.btnBar.style.backgroundColor = config.titleBgColor;
			}
			if(config.bgColor){
				_this.panel = _this.mobileSelect.querySelector('.panel');
				_this.shadowMask = _this.mobileSelect.querySelector('.shadowMask');
				_this.panel.style.backgroundColor = config.bgColor;
				_this.shadowMask.style.background = 'linear-gradient(to bottom, '+ config.bgColor + ', rgba(255, 255, 255, 0), '+ config.bgColor + ')';
			}
			if(!isNaN(config.maskOpacity)){
				_this.grayMask = _this.mobileSelect.querySelector('.grayLayer');
				_this.grayMask.style.background = 'rgba(0, 0, 0, '+ config.maskOpacity +')';
			}
		},

		checkIsPC: function(){
			var _this = this;
			var sUserAgent = navigator.userAgent.toLowerCase();
			var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
			var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
			var bIsMidp = sUserAgent.match(/midp/i) == "midp";
			var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
			var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
			var bIsAndroid = sUserAgent.match(/android/i) == "android";
			var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
			var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";
			if ((bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid || bIsCE || bIsWM)) {
			    _this.isPC = false;
			}
		},

 		show: function(){
			this.mobileSelect.classList.add('mobileSelect-show');
			if (typeof this.onShow === 'function') {
				this.onShow(this);
			}
  		},

	    hide: function() {
			this.mobileSelect.classList.remove('mobileSelect-show');
			if (typeof this.onHide === 'function') {
				this.onHide(this);
			}
	    },

		renderWheels: function(wheelsData, cancelBtnText, ensureBtnText){
			var _this = this;
			var cancelText = cancelBtnText ? cancelBtnText : '取消';
			var ensureText = ensureBtnText ? ensureBtnText : '确认';
			_this.mobileSelect = document.createElement("div");
			_this.mobileSelect.className = "mobileSelect";
			_this.mobileSelect.innerHTML =
		    	'<div class="grayLayer"></div>'+
		        '<div class="content">'+
		            '<div class="btnBar">'+
		                '<div class="fixWidth">'+
		                    '<div class="cancel">'+ cancelText +'</div>'+
		                    '<div class="title"></div>'+
		                    '<div class="ensure">'+ ensureText +'</div>'+
		                '</div>'+
		            '</div>'+
		            '<div class="panel">'+
		                '<div class="fixWidth">'+
		                	'<div class="wheels">'+
			                '</div>'+
		                    '<div class="selectLine"></div>'+
		                    '<div class="shadowMask"></div>'+
		                '</div>'+
		            '</div>'+
		        '</div>';
		    document.body.appendChild(_this.mobileSelect);

			//根据数据长度来渲染

			var tempHTML='';
			for(var i=0; i<wheelsData.length; i++){
			//列
				tempHTML += '<div class="wheel"><ul class="selectContainer">';
				if(_this.jsonType){
					for(var j=0; j<wheelsData[i].data.length; j++){
					//行
						tempHTML += '<li data-id="'+wheelsData[i].data[j][_this.keyMap.id]+'">'+wheelsData[i].data[j][_this.keyMap.value]+'</li>';
					}
				}else{
					for(var j=0; j<wheelsData[i].data.length; j++){
					//行
						tempHTML += '<li>'+wheelsData[i].data[j]+'</li>';
					}
				}
				tempHTML += '</ul></div>';
			}
			_this.mobileSelect.querySelector('.wheels').innerHTML = tempHTML;
		},

		addListenerAll: function(){
			var _this = this;
			for(var i=0; i<_this.slider.length; i++){
				//手势监听
				(function (i) {
					_this.addListenerWheel(_this.wheel[i], i);
				})(i);
			}
		},

		addListenerWheel: function(theWheel, index){
			var _this = this;
			theWheel.addEventListener('touchstart', function () {
				_this.touch(event, this.firstChild, index);
			},false);
			theWheel.addEventListener('touchend', function () {
				_this.touch(event, this.firstChild, index);
			},false);
			theWheel.addEventListener('touchmove', function () {
				_this.touch(event, this.firstChild, index);
			},false);

			if(_this.isPC){
				//如果是PC端则再增加拖拽监听 方便调试
				theWheel.addEventListener('mousedown', function () {
					_this.dragClick(event, this.firstChild, index);
				},false);
				theWheel.addEventListener('mousemove', function () {
					_this.dragClick(event, this.firstChild, index);
				},false);
				theWheel.addEventListener('mouseup', function () {
					_this.dragClick(event, this.firstChild, index);
				},true);
			}
		},

		checkDataType: function(){
			var _this = this;
			if(typeof(_this.wheelsData[0].data[0])=='object'){
				_this.jsonType = true;
			}
		},

		checkCascade: function(){
			var _this = this;
			if(_this.jsonType){
				var node = _this.wheelsData[0].data;
				for(var i=0; i<node.length; i++){
					if(_this.keyMap.childs in node[i] && node[i][_this.keyMap.childs].length > 0){
						_this.cascade = true;
						_this.cascadeJsonData = _this.wheelsData[0].data;
						break;
					}
				}
			}else{
				_this.cascade = false;
			}
		},

		generateArrData: function (targetArr) {
			var tempArr = [];
			var keyMap_id = this.keyMap.id;
			var keyMap_value = this.keyMap.value;
			for(var i=0; i<targetArr.length; i++){
				var tempObj = {};
				tempObj[keyMap_id] = targetArr[i][this.keyMap.id];
				tempObj[keyMap_value] = targetArr[i][this.keyMap.value];
				tempArr.push(tempObj);
			}
			return tempArr;
		},

		initCascade: function(){
			var _this = this;
			_this.displayJson.push(_this.generateArrData(_this.cascadeJsonData));
			if(_this.initPosition.length>0){
				_this.initDeepCount = 0;
				_this.initCheckArrDeep(_this.cascadeJsonData[_this.initPosition[0]]);
			}else{
				_this.checkArrDeep(_this.cascadeJsonData[0]);
			}
			_this.reRenderWheels();
		},

		initCheckArrDeep: function (parent) {
			var _this = this;
			if(parent){
				if (_this.keyMap.childs in parent && parent[_this.keyMap.childs].length > 0) {
					_this.displayJson.push(_this.generateArrData(parent[_this.keyMap.childs]));
					_this.initDeepCount++;
					var nextNode = parent[_this.keyMap.childs][_this.initPosition[_this.initDeepCount]];
					if(nextNode){
						_this.initCheckArrDeep(nextNode);
					}else{
						_this.checkArrDeep(parent[_this.keyMap.childs][0]);
					}
				}
			}
		},

		checkArrDeep: function (parent) {
			//检测子节点深度  修改 displayJson
			var _this = this;
			if(parent){
				if (_this.keyMap.childs in parent && parent[_this.keyMap.childs].length > 0) {
					_this.displayJson.push(_this.generateArrData(parent[_this.keyMap.childs])); //生成子节点数组
					_this.checkArrDeep(parent[_this.keyMap.childs][0]);//检测下一个子节点
				}
			}
		},

		checkRange: function(index, posIndexArr){
			var _this = this;
			var deleteNum = _this.displayJson.length-1-index;
			for(var i=0; i<deleteNum; i++){
				_this.displayJson.pop(); //修改 displayJson
			}
			var resultNode;
			for (var i = 0; i <= index; i++){
				if (i == 0)
					resultNode = _this.cascadeJsonData[posIndexArr[0]];
				else {
					resultNode = resultNode[_this.keyMap.childs][posIndexArr[i]];
				}
			}
			_this.checkArrDeep(resultNode);
			//console.log(_this.displayJson);
			_this.reRenderWheels();
			_this.fixRowStyle();
			_this.setCurDistance(_this.resetPosition(index, posIndexArr));
		},

		resetPosition: function(index, posIndexArr){
			var _this = this;
			var tempPosArr = posIndexArr;
			var tempCount;
			if(_this.slider.length > posIndexArr.length){
				tempCount = _this.slider.length - posIndexArr.length;
				for(var i=0; i<tempCount; i++){
					tempPosArr.push(0);
				}
			}else if(_this.slider.length < posIndexArr.length){
				tempCount = posIndexArr.length - _this.slider.length;
				for(var i=0; i<tempCount; i++){
					tempPosArr.pop();
				}
			}
			for(var i=index+1; i< tempPosArr.length; i++){
				tempPosArr[i] = 0;
			}
			return tempPosArr;
		},

		reRenderWheels: function(){
			var _this = this;
			//删除多余的wheel
			if(_this.wheel.length > _this.displayJson.length){
				var count = _this.wheel.length - _this.displayJson.length;
				for(var i=0; i<count; i++){
					_this.wheels.removeChild(_this.wheel[_this.wheel.length-1]);
				}
			}
			for(var i=0; i<_this.displayJson.length; i++){
			//列
				(function (i) {
					var tempHTML='';
					if(_this.wheel[i]){
						//console.log('插入Li');
						for(var j=0; j<_this.displayJson[i].length; j++){
						//行
							tempHTML += '<li data-id="'+_this.displayJson[i][j][_this.keyMap.id]+'">'+_this.displayJson[i][j][_this.keyMap.value]+'</li>';
						}
						_this.slider[i].innerHTML = tempHTML;

					}else{
						var tempWheel = document.createElement("div");
						tempWheel.className = "wheel";
						tempHTML = '<ul class="selectContainer">';
						for(var j=0; j<_this.displayJson[i].length; j++){
						//行
							tempHTML += '<li data-id="'+_this.displayJson[i][j][_this.keyMap.id]+'">'+_this.displayJson[i][j][_this.keyMap.value]+'</li>';
						}
						tempHTML += '</ul>';
						tempWheel.innerHTML = tempHTML;

						_this.addListenerWheel(tempWheel, i);
				    	_this.wheels.appendChild(tempWheel);
					}
					//_this.·(i);
				})(i);
			}
		},

		updateWheels:function(data){
			var _this = this;
			if(_this.cascade){
				_this.cascadeJsonData = data;
				_this.displayJson = [];
				_this.initCascade();
				if(_this.initPosition.length < _this.slider.length){
					var diff = _this.slider.length - _this.initPosition.length;
					for(var i=0; i<diff; i++){
						_this.initPosition.push(0);
					}
				}
				_this.setCurDistance(_this.initPosition);
				_this.fixRowStyle();
			}
		},

		updateWheel: function(sliderIndex, data){
			var _this = this;
			var tempHTML='';
	    	if(_this.cascade){
	    		console.error('级联格式不支持updateWheel(),请使用updateWheels()更新整个数据源');
				return false;
	    	}
	    	else if(_this.jsonType){
				for(var j=0; j<data.length; j++){
					tempHTML += '<li data-id="'+data[j][_this.keyMap.id]+'">'+data[j][_this.keyMap.value]+'</li>';
				}
				_this.wheelsData[sliderIndex] = {data: data};
	    	}else{
				for(var j=0; j<data.length; j++){
					tempHTML += '<li>'+data[j]+'</li>';
				}
				_this.wheelsData[sliderIndex] = data;
	    	}
			_this.slider[sliderIndex].innerHTML = tempHTML;
		},

		fixRowStyle: function(){
			var _this = this;
			var width = (100/_this.wheel.length).toFixed(2);
			for(var i=0; i<_this.wheel.length; i++){
				_this.wheel[i].style.width = width+'%';
			}
		},

	    getIndex: function(distance){
	        return Math.round((2*this.liHeight-distance)/this.liHeight);
	    },

	    getIndexArr: function(){
	    	var _this = this;
	    	var temp = [];
	    	for(var i=0; i<_this.curDistance.length; i++){
	    		temp.push(_this.getIndex(_this.curDistance[i]));
	    	}
	    	return temp;
	    },

	    getCurValue: function(){
	    	var _this = this;
	    	var temp = [];
	    	var positionArr = _this.getIndexArr();
	    	if(_this.cascade){
		    	for(var i=0; i<_this.wheel.length; i++){
		    		temp.push(_this.displayJson[i][positionArr[i]]);
		    	}
	    	}
	    	else if(_this.jsonType){
		    	for(var i=0; i<_this.curDistance.length; i++){
		    		temp.push(_this.wheelsData[i].data[_this.getIndex(_this.curDistance[i])]);
		    	}
	    	}else{
		    	for(var i=0; i<_this.curDistance.length; i++){
		    		temp.push(_this.getInnerHtml(i));
		    	}
	    	}
	    	return temp;
	    },

	    getValue: function(){
	    	return this.curValue;
	    },

	    calcDistance: function(index){
			return 2*this.liHeight-index*this.liHeight;
	    },

	    setCurDistance: function(indexArr){
	    	var _this = this;
	    	var temp = [];
	    	for(var i=0; i<_this.slider.length; i++){
	    		temp.push(_this.calcDistance(indexArr[i]));
	    		_this.movePosition(_this.slider[i],temp[i]);
	    	}
	    	_this.curDistance = temp;
	    },

	    fixPosition: function(distance){
	        return -(this.getIndex(distance)-2)*this.liHeight;
	    },

	    movePosition: function(theSlider, distance){
	        theSlider.style.webkitTransform = 'translate3d(0,' + distance + 'px, 0)';
	        theSlider.style.transform = 'translate3d(0,' + distance + 'px, 0)';
	    },

	    locatePosition: function(index, posIndex){
	    	var _this = this;
  	    	this.curDistance[index] = this.calcDistance(posIndex);
  	    	this.movePosition(this.slider[index],this.curDistance[index]);
	        if(_this.cascade){
		    	_this.checkRange(index, _this.getIndexArr());
			}
	    },

	    updateCurDistance: function(theSlider, index){
	        if(theSlider.style.transform){
				this.curDistance[index] = parseInt(theSlider.style.transform.split(',')[1]);
	        }else{
				this.curDistance[index] = parseInt(theSlider.style.webkitTransform.split(',')[1]);
	        }
	    },

	    getDistance:function(theSlider){
	    	if(theSlider.style.transform){
	    		return parseInt(theSlider.style.transform.split(',')[1]);
	    	}else{
	    		return parseInt(theSlider.style.webkitTransform.split(',')[1]);
	    	}
	    },

	    getInnerHtml: function(sliderIndex){
	    	var _this = this;
	    	var index = _this.getIndex(_this.curDistance[sliderIndex]);
	    	return "<span>"+_this.slider[sliderIndex].getElementsByTagName('li')[index].innerHTML+"<tt>:</tt></span>"
	    },

	    touch: function(event, theSlider, index){
	    	var _this = this;
	    	event = event || window.event;
	    	switch(event.type){
	    		case "touchstart":
			        _this.startY = event.touches[0].clientY;
			        _this.startY = parseInt(_this.startY);
			        _this.oldMoveY = _this.startY;
	    			break;

	    		case "touchend":

			        _this.moveEndY = parseInt(event.changedTouches[0].clientY);
			        _this.offsetSum = _this.moveEndY - _this.startY;
					_this.oversizeBorder = -(theSlider.getElementsByTagName('li').length-3)*_this.liHeight;

					if(_this.offsetSum == 0){
						//offsetSum为0,相当于点击事件
						// 0 1 [2] 3 4
						var clickOffetNum = parseInt((document.documentElement.clientHeight - _this.moveEndY)/40);
						if(clickOffetNum!=2){
							var offset = clickOffetNum - 2;
							var newDistance = _this.curDistance[index] + (offset*_this.liHeight);
							if((newDistance <= 2*_this.liHeight) && (newDistance >= _this.oversizeBorder) ){
								_this.curDistance[index] = newDistance;
								_this.movePosition(theSlider, _this.curDistance[index]);
								_this.transitionEnd(_this.getIndexArr(),_this.getCurValue());
							}
						}
					}else{
						//修正位置
						_this.updateCurDistance(theSlider, index);
						_this.curDistance[index] = _this.fixPosition(_this.curDistance[index]);
						_this.movePosition(theSlider, _this.curDistance[index]);

				        //反弹
				        if(_this.curDistance[index] + _this.offsetSum > 2*_this.liHeight){
				            _this.curDistance[index] = 2*_this.liHeight;
				            setTimeout(function(){
				                _this.movePosition(theSlider, _this.curDistance[index]);
				            }, 100);

				        }else if(_this.curDistance[index] + _this.offsetSum < _this.oversizeBorder){
				            _this.curDistance[index] = _this.oversizeBorder;
				            setTimeout(function(){
				                _this.movePosition(theSlider, _this.curDistance[index]);
				            }, 100);
				        }
						_this.transitionEnd(_this.getIndexArr(),_this.getCurValue());
					}

 			        if(_this.cascade){
				        _this.checkRange(index, _this.getIndexArr());
				    }

	    			break;

	    		case "touchmove":
			        event.preventDefault();
			        _this.moveY = event.touches[0].clientY;
			        _this.offset = _this.moveY - _this.oldMoveY;

			        _this.updateCurDistance(theSlider, index);
			        _this.curDistance[index] = _this.curDistance[index] + _this.offset;
			        _this.movePosition(theSlider, _this.curDistance[index]);
			        _this.oldMoveY = _this.moveY;
	    			break;
	    	}
	    },

	    dragClick: function(event, theSlider, index){
	    	var _this = this;
	    	event = event || window.event;
	    	switch(event.type){
	    		case "mousedown":
			        _this.startY = event.clientY;
			        _this.oldMoveY = _this.startY;
			        _this.clickStatus = true;
	    			break;

	    		case "mouseup":

			        _this.moveEndY = event.clientY;
			        _this.offsetSum = _this.moveEndY - _this.startY;
					_this.oversizeBorder = -(theSlider.getElementsByTagName('li').length-3)*_this.liHeight;

					if(_this.offsetSum == 0){
						var clickOffetNum = parseInt((document.documentElement.clientHeight - _this.moveEndY)/40);
						if(clickOffetNum!=2){
							var offset = clickOffetNum - 2;
							var newDistance = _this.curDistance[index] + (offset*_this.liHeight);
							if((newDistance <= 2*_this.liHeight) && (newDistance >= _this.oversizeBorder) ){
								_this.curDistance[index] = newDistance;
								_this.movePosition(theSlider, _this.curDistance[index]);
								_this.transitionEnd(_this.getIndexArr(),_this.getCurValue());
							}
						}
					}else{
						//修正位置
						_this.updateCurDistance(theSlider, index);
						_this.curDistance[index] = _this.fixPosition(_this.curDistance[index]);
						_this.movePosition(theSlider, _this.curDistance[index]);

						//反弹
						if(_this.curDistance[index] + _this.offsetSum > 2*_this.liHeight){
						    _this.curDistance[index] = 2*_this.liHeight;
						    setTimeout(function(){
						        _this.movePosition(theSlider, _this.curDistance[index]);
						    }, 100);

						}else if(_this.curDistance[index] + _this.offsetSum < _this.oversizeBorder){
						    _this.curDistance[index] = _this.oversizeBorder;
						    setTimeout(function(){
						        _this.movePosition(theSlider, _this.curDistance[index]);
						    }, 100);
						}
						_this.transitionEnd(_this.getIndexArr(),_this.getCurValue());

					}

			        _this.clickStatus = false;
 			        if(_this.cascade){
				        _this.checkRange(index, _this.getIndexArr());
			    	}
	    			break;

	    		case "mousemove":
			        event.preventDefault();
			        if(_this.clickStatus){
				        _this.moveY = event.clientY;
				        _this.offset = _this.moveY - _this.oldMoveY;
				        _this.updateCurDistance(theSlider, index);
				        _this.curDistance[index] = _this.curDistance[index] + _this.offset;
				        _this.movePosition(theSlider, _this.curDistance[index]);
				        _this.oldMoveY = _this.moveY;
			        }
	    			break;
	    	}
	    }

	};

	if (typeof exports == "object") {
		module.exports = MobileSelect;
	} else if (typeof define == "function" && define.amd) {
		define([], function () {
			return MobileSelect;
		})
	} else {
		window.MobileSelect = MobileSelect;
	}
})();

//封装函数
/**
 * 参数说明
 * @param trigger(必填参数) 触发对象的id/class/tag
 * @param wheels(必填参数)  数据源,需要显示的数据
 * @param title 控件标题
 * @param position 初始化定位
 * @param callback 选择成功后触发的回调函数，返回indexArr(选中的选项索引)、data(选中的数据)
 * @param transitionEnd 每一次手势滑动结束后触发的回调函数,返回indexArr(当前选中的选项索引)、data(选中的数据)
 */
function mobiletime(param){
var numArr1=param.shi||['0','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','23'];
var numArr2=param.fen||['0','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31','32','33','34','35','36','37','38','39','40','41','42','43','44','45','46','47','48','49','50','51','52','53','54','55','56','57','58','59'];
var numArr3=param.miao||['0','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31','32','33','34','35','36','37','38','39','40','41','42','43','44','45','46','47','48','49','50','51','52','53','54','55','56','57','58','59'];
var mobileSelect3 = new MobileSelect({
    trigger: param.trigger||'#trigger3',
    title: param.title||'选择时间',
    wheels: [
                {data: numArr1},
                {data: numArr2},
                {data: numArr3}
            ],
    position:[0, 0, 0],
    transitionEnd:function(indexArr, data){
        console.log(data);
        if(typeof  param.transitionEnd=='function'){
       param.transitionEnd(indexArr,data);
        }
    },
    callback:function(indexArr, data){
     	//$("#trigger3").html("ccccccccccccccccccccc");
     	 if(typeof  param.callback=='function'){
        param.callback(indexArr,data);
        }
    }
});
}