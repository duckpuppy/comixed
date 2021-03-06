/*
 * ComixEd - A digital comic book library management application.
 * Copyright (C) 2017, Darryl L. Pierce
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.package
 * org.comixed;
 */

package org.comixed.library.model;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.comixed.library.adaptors.ArchiveAdaptorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Page</code> represents a single page from a comic.
 *
 * @author Darryl L. Pierce
 *
 */
@Entity
@Table(name = "pages")
@NamedQueries(
{@NamedQuery(name = "Page.getDuplicatePageList",
             query = "SELECT p FROM Page p WHERE p.hash IN (SELECT d.hash FROM Page d GROUP BY d.hash HAVING COUNT(*) > 1)"),
 @NamedQuery(name = "Page.getDuplicatePageCount",
             query = "SELECT COUNT(p) FROM Page p WHERE p.hash IN (SELECT d.hash FROM Page d GROUP BY d.hash HAVING COUNT(*) > 1)"),})
public class Page
{
    private static final String MISSING_PAGE_URL = "/images/missing.png";
    public static Page MISSING_PAGE = null;

    static
    {
        try
        {
            byte[] content = IOUtils.resourceToByteArray(MISSING_PAGE_URL);
            MISSING_PAGE = new Page("", content);
        }
        catch (IOException error)
        {
            throw new RuntimeException("Failed to load resource file", error);
        }
    }

    public static String createImageCacheKey(int width, int height)
    {
        return String.valueOf(width) + "x" + String.valueOf(height);
    }

    @Transient
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @Column(name = "filename",
            updatable = true,
            nullable = false)
    private String filename;

    @Column(name = "hash",
            updatable = true,
            nullable = false)
    private String hash;

    @Column(name = "deleted",
            updatable = true,
            nullable = false)
    private boolean deleted = false;

    @Transient
    private byte[] content;

    @Transient
    private Image icon;

    @Transient
    protected Map<String,
                  Image> imageCache = new WeakHashMap<>();

    /**
     * Default constructor.
     */
    public Page()
    {}

    /**
     * Creates a new instance with the given filename and image content.
     *
     * @param filename
     *            the filename
     * @param content
     *            the content
     */
    public Page(String filename, byte[] content)
    {
        this.logger.debug("Creating page: filename=" + filename + " content.size=" + content.length);
        this.filename = filename;
        this.content = content;
        this.hash = this.createHash(content);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        Page other = (Page )obj;
        if (this.filename == null)
        {
            if (other.filename != null) return false;
        }
        else if (!this.filename.equals(other.filename)) return false;
        if (this.hash == null)
        {
            if (other.hash != null) return false;
        }
        else if (!this.hash.equals(other.hash)) return false;
        return true;
    }

    /**
     * Returns the owning comic.
     *
     * @return the comic
     */
    public Comic getComic()
    {
        return this.comic;
    }

    /**
     * Returns the content for the page.
     *
     * @return the content
     */
    public byte[] getContent()
    {
        if (this.content == null)
        {
            this.logger.debug("Loading page image: filename=" + this.filename);
            try
            {
                if (this.comic.archiveType != null)
                {
                    this.content = this.comic.archiveType.getArchiveAdaptor().loadSingleFile(this.comic, this.filename);
                }
            }
            catch (ArchiveAdaptorException error)
            {
                this.logger.warn("failed to load entry: " + this.filename + " comic=" + this.comic.getFilename(),
                                 error);
            }
        }
        return this.content;
    }

    /**
     * Returns the filename for the page.
     *
     * @return the filename
     */
    public String getFilename()
    {
        return this.filename;
    }

    public String getHash()
    {
        return this.hash;
    }

    /**
     * Returns the original image for the page.
     *
     * @return the image
     */
    public Image getImage()
    {
        if (this.icon == null)
        {
            this.logger.debug("Generating image from content");
            try
            {
                this.icon = ImageIO.read(new ByteArrayInputStream(this.getContent()));
            }
            catch (IOException error)
            {
                this.logger.error("Failed to load image from " + this.comic.getFilename(), error);
            }
        }
        return this.icon;
    }

    /**
     * Returns a scaled copy of the page image.
     *
     * @param maxWidth
     *            the maximum scaled width
     * @param maxHeight
     *            the maximum scaled height
     * @return the scaled image
     */
    public Image getImage(int maxWidth, int maxHeight)
    {
        this.logger.debug("Scaling page: maxWidth=" + maxWidth + ", maxHeight=" + maxHeight);
        Image image = this.getImage();

        int boundWidth = maxWidth;
        int boundHeight = maxHeight;
        int oldWidth = image.getWidth(null);
        int oldHeight = image.getHeight(null);

        this.logger.debug("oldWidth=" + oldWidth);
        this.logger.debug("oldHeight=" + oldHeight);

        if ((boundWidth < 1) && (boundHeight < 1))
        {
            this.logger.debug("If both maxWidth and maxHeight are less than 1, then consider using getImage()");
            boundWidth = oldWidth;
            boundHeight = oldHeight;
        }
        else if (boundWidth < 1)
        {
            boundWidth = (int )(((float )oldWidth * (float )boundHeight) / oldHeight);
        }
        else if (boundHeight < 1)
        {
            boundHeight = (int )(((float )oldHeight * (float )boundWidth) / oldWidth);
        }

        Image result = null;
        String key = Page.createImageCacheKey(boundWidth, boundHeight);

        if (this.imageCache.containsKey(key))
        {
            this.logger.debug("Found image in cache: (" + boundWidth + "x" + boundHeight + ")");
            result = this.imageCache.get(key);
        }
        else
        {
            this.logger.debug("Scaling image: old=(" + oldWidth + "x" + oldHeight + ") new=(" + boundWidth + "x"
                              + boundHeight + ")");
            result = image.getScaledInstance(boundWidth, boundHeight, Image.SCALE_SMOOTH);
            this.logger.debug("Placing scaled image into cache");
            this.imageCache.put(key, result);
        }

        return result;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.filename == null) ? 0 : this.filename.hashCode());
        result = (prime * result) + ((this.hash == null) ? 0 : this.hash.hashCode());
        return result;
    }

    /**
     * Returns if the page is marked for deletion.
     *
     * @return true if marked for deletion
     */
    public boolean isMarkedDeleted()
    {
        return this.deleted;
    }

    /**
     * Sets the deleted flag for the page.
     *
     * @param deleted
     *            true if the page is to be deleted
     */
    public void markDeleted(boolean deleted)
    {
        this.logger.debug("Mark deletion: " + deleted);
        this.deleted = deleted;
    }

    /**
     * Sets a new filename for the page.
     *
     * @param filename
     *            the new filename
     */
    public void setFilename(String filename)
    {
        this.logger.debug("Changing filename: " + this.filename + " -> " + filename);
        this.filename = filename;
    }

    private String createHash(byte[] bytes)
    {
        this.logger.debug("Generating MD5 hash");
        String result = "";
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            result = new BigInteger(1, md.digest()).toString(16).toUpperCase();
        }
        catch (NoSuchAlgorithmException error)
        {
            this.logger.error("Failed to generate hash", error);
        }
        this.logger.debug("Returning: " + result);
        return result;
    }

    void setComic(Comic comic)
    {
        this.comic = comic;
    }
}
