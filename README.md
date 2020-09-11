# EPSON-printer-command-Springboot
Discription:
Using springboot as framework to boot up a application that directly sends printing command (ESC/P).

<br>Specification:<br>
<br>Framework:sprintboot<br>
<br>Local Printer:EPSON LQ690C<br>
<br>Queue server: RabbitMq<br>

Flow:<br/>
1.Client sets up printer locally, and downloads spring-boot application.<br/>
2.Spring-boot application requires basic information.<br/>
3.After the 'receive' button clicked, the application starts listening.<br/>
4.If there is any message(in JSON form) on the RabbitMq server, the application actively receives printing content from RabbitMq server.<br/>
5.Once the message from server received, application starts producing printing command(which directly send to the local set-up printer)<br/>
