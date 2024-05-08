# MailVerify

MailVerify, Minecraft sunucularında oyuncuların e-posta adreslerini doğrulamak için kullanılan bir Bukkit/Spigot eklentisidir. Bu eklenti, oyuncuların sunucuya giriş yapmadan önce e-posta adreslerini kaydetmelerini ve doğrulamalarını sağlar.

## Kurulum

1. Sunucunuzun çalıştığı dizindeki `plugins` klasörüne `MailVerify.jar` dosyasını kopyalayın.
2. Sunucuyu yeniden başlatın.

## Kullanım

Oyuncular, sunucuya giriş yaptıktan sonra `/eposta <e-posta>` komutunu kullanarak e-posta adreslerini kaydedebilirler. E-posta adresi, oyuncunun sunucuya giriş yapmadan önce doğrulaması gerekmektedir. E-posta adresi doğrulandıktan sonra, oyuncu sunucuya giriş yapabilir.

## Yapılandırma

Eklenti, `config.yml` dosyasında özelleştirilebilir. Bu dosyada eklenti için kullanılan mesajlar ve MySQL veritabanı bağlantı bilgileri bulunmaktadır.
