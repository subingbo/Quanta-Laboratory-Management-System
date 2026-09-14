package com.ruoyi.common.utils.file;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.file.InvalidExtensionException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 文件内容校验：大小档位、魔数（真实类型）、图片像素上限。
 * <p>
 * 只看文件后缀是可绕过的（把 html 改名成 png 即可上传成功），因此这里统一以文件头字节为准，
 * 并要求后缀与真实类型一致。
 *
 * @author Quanta
 */
public class FileValidator
{
    /** 图片类档位：证件照 / 头像 / 收款码 / 效果图 / 支付凭证 */
    public static final long SIZE_IMAGE = 5L * 1024 * 1024;

    /** 文档类档位：学习资料（pdf / word / excel / ppt / 压缩包） */
    public static final long SIZE_DOCUMENT = 20L * 1024 * 1024;

    /** 表格导入档位 */
    public static final long SIZE_IMPORT = 5L * 1024 * 1024;

    /** 图片单边像素上限 */
    public static final int MAX_IMAGE_EDGE = 4096;

    /** 图片总像素上限（防止高压缩比巨图打爆解码内存） */
    public static final long MAX_IMAGE_PIXELS = 20_000_000L;

    private static final String[] IMAGE_KINDS = { "bmp", "gif", "jpg", "jpeg", "png" };

    /**
     * 校验大小、真实类型与图片像素。
     *
     * @param file 上传文件
     * @param allowedExtension 允许的后缀白名单
     * @param maxSize 该场景的字节上限
     * @throws InvalidExtensionException 类型/内容不合法
     */
    public static void validate(MultipartFile file, String[] allowedExtension, long maxSize)
            throws InvalidExtensionException
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传内容为空");
        }
        if (file.getSize() > maxSize)
        {
            throw new ServiceException(StringUtils.format("文件大小超出限制，最大允许 {}MB", maxSize / 1024 / 1024));
        }

        String extension = FileUploadUtils.getExtension(file);
        if (StringUtils.isEmpty(extension))
        {
            throw new ServiceException("无法识别文件类型，请确认文件带有正确的扩展名");
        }
        String lower = extension.toLowerCase(Locale.ROOT);
        if (!FileUploadUtils.isAllowedExtension(lower, allowedExtension))
        {
            throw new InvalidExtensionException(allowedExtension, extension, file.getOriginalFilename());
        }

        byte[] head = readHead(file, 16);
        if (!matchesKind(lower, head, file.getContentType()))
        {
            throw new ServiceException(StringUtils.format("文件内容不是合法的 {} 格式（疑似改名文件），已拒绝上传",
                    lower.toUpperCase(Locale.ROOT)));
        }

        if (isImage(lower))
        {
            assertImageDimension(file);
        }
    }

    /**
     * 只校验「内容与后缀一致 + 图片像素」，不做大小与白名单判断。
     * 供 {@link FileUploadUtils#assertAllowed} 在其自身校验之后调用。
     */
    public static void validateContent(MultipartFile file, String[] allowedExtension)
    {
        String extension = FileUploadUtils.getExtension(file);
        if (StringUtils.isEmpty(extension))
        {
            throw new ServiceException("无法识别文件类型，请确认文件带有正确的扩展名");
        }
        String lower = extension.toLowerCase(Locale.ROOT);
        if (allowedExtension != null && !FileUploadUtils.isAllowedExtension(lower, allowedExtension))
        {
            return;
        }
        byte[] head = readHead(file, 16);
        if (!matchesKind(lower, head, file.getContentType()))
        {
            throw new ServiceException(StringUtils.format("文件内容不是合法的 {} 格式（疑似改名文件），已拒绝上传",
                    lower.toUpperCase(Locale.ROOT)));
        }
        if (isImage(lower))
        {
            assertImageDimension(file);
        }
    }

    /** 仅校验大小（供已在别处做类型校验的入口复用） */
    public static void assertSize(MultipartFile file, long maxSize)
    {
        if (file != null && file.getSize() > maxSize)
        {
            throw new ServiceException(StringUtils.format("文件大小超出限制，最大允许 {}MB", maxSize / 1024 / 1024));
        }
    }

    private static boolean isImage(String extension)
    {
        for (String image : IMAGE_KINDS)
        {
            if (image.equals(extension))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 图片像素上限校验。只读文件头拿宽高，不做整图解码。
     */
    private static void assertImageDimension(MultipartFile file)
    {
        try (InputStream in = file.getInputStream(); ImageInputStream iis = ImageIO.createImageInputStream(in))
        {
            if (iis == null)
            {
                return;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext())
            {
                return;
            }
            ImageReader reader = readers.next();
            try
            {
                reader.setInput(iis, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width > MAX_IMAGE_EDGE || height > MAX_IMAGE_EDGE)
                {
                    throw new ServiceException(StringUtils.format("图片尺寸过大（{}×{}），单边最大 {} 像素", width, height,
                            MAX_IMAGE_EDGE));
                }
                if ((long) width * height > MAX_IMAGE_PIXELS)
                {
                    throw new ServiceException(StringUtils.format("图片像素过多（{}×{}），请先压缩后再上传", width, height));
                }
            }
            finally
            {
                reader.dispose();
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            // 图片解析不了本身就是可疑内容
            throw new ServiceException(StringUtils.format("图片内容无法解析，已拒绝上传：{}", e.getMessage()));
        }
    }

    /**
     * 按后缀比对文件头字节。
     */
    private static boolean matchesKind(String extension, byte[] head, String contentType)
    {
        switch (extension)
        {
            case "jpg":
            case "jpeg":
                return startsWith(head, 0xFF, 0xD8, 0xFF);
            case "png":
                return startsWith(head, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif":
                return text(head, 0, "GIF8");
            case "bmp":
                return text(head, 0, "BM");
            case "pdf":
                return text(head, 0, "%PDF");
            // 压缩容器：docx/xlsx/pptx 本质是 zip
            case "zip":
            case "docx":
            case "xlsx":
            case "pptx":
                return startsWith(head, 0x50, 0x4B, 0x03, 0x04) || startsWith(head, 0x50, 0x4B, 0x05, 0x06)
                        || startsWith(head, 0x50, 0x4B, 0x07, 0x08);
            case "rar":
                return text(head, 0, "Rar!");
            case "gz":
            case "tgz":
                return startsWith(head, 0x1F, 0x8B);
            case "bz2":
                return text(head, 0, "BZh");
            // 老式 OLE 复合文档：doc/xls/ppt
            case "doc":
            case "xls":
            case "ppt":
                return startsWith(head, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            case "mp4":
            case "avi":
            case "rm":
            case "rmvb":
            case "mp3":
            case "wav":
            case "wma":
            case "wmv":
            case "mid":
            case "asf":
            case "swf":
            case "flv":
                return matchesMedia(head, contentType);
            case "txt":
                return looksLikeText(head, contentType);
            default:
                return false;
        }
    }

    private static boolean matchesMedia(byte[] head, String contentType)
    {
        if (text(head, 0, "RIFF") || text(head, 4, "ftyp") || text(head, 0, ".RMF") || text(head, 0, "ID3")
                || text(head, 0, "FLV") || text(head, 0, "CWS") || text(head, 0, "FWS") || text(head, 0, "ZWS"))
        {
            return true;
        }
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("video/");
    }

    /**
     * txt 没有文件头签名，因此要求声明为 text/*，且前 16 字节内不出现 NUL（二进制特征）。
     */
    private static boolean looksLikeText(byte[] head, String contentType)
    {
        for (byte b : head)
        {
            if (b == 0x00)
            {
                return false;
            }
        }
        return contentType == null || contentType.toLowerCase(Locale.ROOT).startsWith("text/")
                || "application/octet-stream".equalsIgnoreCase(contentType);
    }

    private static byte[] readHead(MultipartFile file, int len)
    {
        byte[] buf = new byte[len];
        try (InputStream in = file.getInputStream())
        {
            int read = 0;
            while (read < len)
            {
                int n = in.read(buf, read, len - read);
                if (n < 0)
                {
                    break;
                }
                read += n;
            }
            return read < len ? Arrays.copyOf(buf, read) : buf;
        }
        catch (Exception e)
        {
            return new byte[0];
        }
    }

    private static boolean startsWith(byte[] head, int... signature)
    {
        if (head.length < signature.length)
        {
            return false;
        }
        for (int i = 0; i < signature.length; i++)
        {
            if ((head[i] & 0xFF) != (signature[i] & 0xFF))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean text(byte[] head, int offset, String expected)
    {
        if (head.length < offset + expected.length())
        {
            return false;
        }
        for (int i = 0; i < expected.length(); i++)
        {
            if ((char) (head[offset + i] & 0xFF) != expected.charAt(i))
            {
                return false;
            }
        }
        return true;
    }
}
