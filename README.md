# unique-export

* 20250402 add self defined headers
* 20250331 add excel export demo

# demo download 
``
   byte[] byteArray = byteArrayOutputStream.toByteArray();
   response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("测试.xlsx", "UTF-8"));
   response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
   response.setContentLength(byteArray.length);
   response.getOutputStream().write(byteArray);
``
