# PasswordInputView
自定义的密码、验证码输入框  

### 使用方法
#### 布局使用

	<com.example.administrator.anywaydemo.customview.view.PasswordInputView
        android:id="@+id/password_input_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:border_color="#BBBBBB"
        app:border_margin="10dp"
        app:border_width="1dp"
        app:border_style="square_separable"
        app:cursor_color="#69B56D"
        app:cursor_height="28dp"
        app:cursor_width="2dp"
        app:number_count="6"
        app:select_border_color="#69B56D"
        app:side_length="45dp"
        app:text_color="#69B56D"
        app:text_size="30sp"
        app:input_type="password"/>

#### 监听使用

	PasswordInputView passwordInputView = findViewById(R.id.password_input_view);

    passwordInputView.setOnInputFinishListener(new PasswordInputView.OnInputFinishListener() {
        @Override
        public void onInputFinish(String inputStr) {
			// 输入字符个数达到number_count后，此方法会被回调
            Log.e("PasswordInputView", "inputStr：" + inputStr);
        }
    });

### 相关属性介绍  

	number_count：输入框个数  
	side_length：输入框宽度  
	text_size：字体尺寸  
	text_color：字体颜色  
	cursor_width：光标宽度  
	cursor_height：光标长度  
	cursor_color：光标颜色  
	border_color：边框颜色  
	border_margin：边框间距    
	border_ width：边框宽度  
	select_border_color：当前边框颜色  
	input_type：输入类型（密码和验证码）
	border_style：边框类型

### 展示
![图片](https://github.com/fliet/PasswordInputView/blob/master/app/src/main/res/drawable/pic_1.jpg)
