package com.minkey.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码生成
 */
public class VCodeUtil {
    private static int width = 70;// 定义图片的width
    private static int height = 45;// 定义图片的height
    public static final int VCODE_NUM = 4;// 定义图片上显示验证码的个数
    private static int startX = 6;//验证码开始的横向x值
    private static int codeX = 14;//每个验证码本身所占的x值
    private static  int codeY = 27;//验证码所在的y值
    private static int fontHeight = 20;
    private static char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };


    public static char[] getCode(int num){
        char[] vcode = new char[num];
        // 创建一个随机数生成器类
        Random random = new Random();

        for(int i = 0 ;i<num ; i++){
            vcode[i] = codeSequence[random.nextInt(codeSequence.length)];
        }
        return vcode;
    }

    /**
     * 生成一个map集合
     * code为生成的验证码
     * codePic为生成的验证码BufferedImage对象
     * @return
     */
    public static BufferedImage generateCodeAndPic(char[] vcode) {
        // 定义图像buffer
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Graphics2D gd = buffImg.createGraphics();
        // Graphics2D gd = (Graphics2D) buffImg.getGraphics();
        Graphics gd = buffImg.getGraphics();
        // 创建一个随机数生成器类
        Random random = new Random();
        // 将图像填充为白色
        gd.setColor(Color.WHITE);
        gd.fillRect(0, 0, width, height);

        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        // 设置字体。
        gd.setFont(font);

        // 画边框。
        gd.setColor(Color.BLACK);
        gd.drawRect(0, 0, width - 1, height - 1);

        // 随机产生N条干扰线，使图象中的认证码不易被其它程序探测到。
        gd.setColor(Color.BLACK);
        for (int i = 0; i < 2; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x, y, x + xl, y + yl);
        }

        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        StringBuffer randomCode = new StringBuffer();
        int red = 0, green = 0, blue = 0;

        // 随机产生codeCount数字的验证码。
        for (int i = 0; i < vcode.length; i++) {
            // 得到随机产生的验证码数字。
            String code = String.valueOf(vcode[i]);
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(100);
            green = random.nextInt(100);
            blue = random.nextInt(100);

            // 用随机产生的颜色将验证码绘制到图像中。
            gd.setColor(new Color(red, green, blue));
            gd.drawString(code, startX + i * codeX , codeY);

            // 将产生的四个随机数组合在一起。
            randomCode.append(code);
        }

        return buffImg;
    }

}