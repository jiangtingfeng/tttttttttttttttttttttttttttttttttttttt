package com.jgw.supercodeplatform.trace.controller.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;


import com.jgw.supercodeplatform.trace.common.util.CommonUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/trace/common")
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(tags = "额外接口")
public class CommonController {

    @Autowired
    private CommonUtil commonUtil;

   /**
    * 创建二维码
    * @param content
    * @param response
    * @return
    * @throws WriterException
    * @throws IOException
    */
	@RequestMapping(value = "/qrCode", method = RequestMethod.GET)
	@ApiOperation(value = "生成二维码接口", notes = "")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
        @ApiImplicitParam(name = "content", paramType = "query", defaultValue = "http://www.baidu.com", value = "", required = true),
   })
   public  boolean createQrCode(String content,HttpServletResponse response) throws WriterException, IOException{  
           //设置二维码纠错级别ＭＡＰ
           Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();  
           hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  // 矫错级别  
           QRCodeWriter qrCodeWriter = new QRCodeWriter();  
           //创建比特矩阵(位矩阵)的QR码编码的字符串  
           BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 900, 900, hintMap);  
           // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
           int matrixWidth = byteMatrix.getWidth();  
           BufferedImage image = new BufferedImage(matrixWidth-200, matrixWidth-200, BufferedImage.TYPE_INT_RGB);  
           image.createGraphics();  
           Graphics2D graphics = (Graphics2D) image.getGraphics();  
           graphics.setColor(Color.WHITE);  
           graphics.fillRect(0, 0, matrixWidth, matrixWidth);  
           // 使用比特矩阵画并保存图像
           graphics.setColor(Color.BLACK);  
           for (int i = 0; i < matrixWidth; i++){
               for (int j = 0; j < matrixWidth; j++){
                   if (byteMatrix.get(i, j)){
                       graphics.fillRect(i-100, j-100, 1, 1);  
                   }
               }
           }
           return ImageIO.write(image, "JPEG", response.getOutputStream());  
   }

   @RequestMapping(value = "/download", method = RequestMethod.GET)
   @ApiOperation(value = "下载文件", notes = "")
   @ApiImplicitParams({
           @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
   })
   public InputStreamResource download(String url,String fileName,HttpServletResponse response) {

       File directory = new File("");
       try{
           String root=directory.getAbsolutePath();
           System.out.println(root);

           String path=root+"/files/"+ commonUtil.getUUID()+"/"+fileName;

           FileUtils.copyURLToFile(new URL(url), new File(path));

           response.setContentType("application/octet-stream");
           response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
           InputStreamResource resource = new InputStreamResource(new FileInputStream(path));
           return resource;

       }catch(Exception e){
            e.printStackTrace();
       }

	    return null;
   }

}
