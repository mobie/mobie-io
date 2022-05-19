package org.embl.mobie.io.n5.openers;

import bdv.util.volatiles.SharedQueue;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.*;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Cast;
import org.embl.mobie.io.n5.loaders.N5FSImageLoader;
import org.embl.mobie.io.util.IOHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class N5Opener extends BDVOpener {
    private final String filePath;

    public N5Opener(String filePath) {
        this.filePath = filePath;
    }

    public static SpimData openFile(String filePath, SharedQueue sharedQueue) throws IOException {
        N5Opener omeZarrOpener = new N5Opener(filePath);
        return omeZarrOpener.setSettings(filePath, sharedQueue);
    }

    private static Map<Integer, ViewSetup> createViewSetupsFromXml(final Element sequenceDescription) {
        final HashMap<Integer, ViewSetup> setups = new HashMap<>();
        final HashMap<Integer, Channel> channels = new HashMap<>();
        Element viewSetups = sequenceDescription.getChild("ViewSetups");

        for (final Element elem : viewSetups.getChildren("ViewSetup")) {
            final int id = XmlHelpers.getInt(elem, "id");
            int angleId = 0;
            Angle angle = new Angle(angleId);
            Channel channel = new Channel(angleId);
            Illumination illumination = new Illumination(angleId);
            try {
                final int channelId = XmlHelpers.getInt(elem, "channel");
                channel = channels.get(channelId);
                if (channel == null) {
                    channel = new Channel(channelId);
                    channels.put(channelId, channel);
                }
            } catch (NumberFormatException e) {
            }
            try {
                final String sizeString = elem.getChildText("size");
                final String name = elem.getChildText("name");
                final String[] values = sizeString.split(" ");
                final Dimensions size = new FinalDimensions(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                final String[] voxelValues = elem.getChild("voxelSize").getChildText("size").split(" ");
                final String unit = elem.getChild("voxelSize").getChildText("unit");
                final VoxelDimensions voxelSize = new FinalVoxelDimensions(unit,
                        Double.parseDouble(voxelValues[0]),
                        Double.parseDouble(voxelValues[1]),
                        Double.parseDouble(voxelValues[2]));
                final ViewSetup setup = new ViewSetup(id, name, size, voxelSize, channel, angle, illumination);
                setups.put(id, setup);
            } catch (Exception e) {
                System.out.println("No pixel parameters specified");
            }
        }
        return setups;
    }

    private static TimePoints createTimepointsFromXml(final Element sequenceDescription) {
        final Element timepoints = sequenceDescription.getChild("Timepoints");
        final String type = timepoints.getAttributeValue("type");
        if (type.equals("range")) {
            final int first = Integer.parseInt(timepoints.getChildText("first"));
            final int last = Integer.parseInt(timepoints.getChildText("last"));
            final ArrayList<TimePoint> tps = new ArrayList<>();
            for (int i = first, t = 0; i <= last; ++i, ++t)
                tps.add(new TimePoint(t));
            return new TimePoints(tps);
        } else {
            throw new RuntimeException("unknown <Timepoints> type: " + type);
        }
    }

    public SpimData setSettings(String url, SharedQueue sharedQueue) throws IOException {
        final SAXBuilder sax = new SAXBuilder();
        Document doc;
        try {
            doc = sax.build( IOHelper.getInputStream(url));
            final Element root = doc.getRootElement();
            final Element sequenceDescriptionElement = root.getChild("SequenceDescription");
            final Element imageLoaderElement = sequenceDescriptionElement.getChild("ImageLoader");
            final TimePoints timepoints = createTimepointsFromXml(sequenceDescriptionElement);
            final Map<Integer, ViewSetup> setups = createViewSetupsFromXml(sequenceDescriptionElement);
            final MissingViews missingViews = null;
            final Element viewRegistrations = root.getChild("ViewRegistrations");
            final ArrayList<ViewRegistration> regs = new ArrayList<>();
            for (final Element vr : viewRegistrations.getChildren("ViewRegistration")) {
                final int timepointId = Integer.parseInt(vr.getAttributeValue("timepoint"));
                final int setupId = Integer.parseInt(vr.getAttributeValue("setup"));
                final AffineTransform3D transform = new AffineTransform3D();
                transform.set(XmlHelpers.getDoubleArray(vr.getChild("ViewTransform"), "affine"));
                regs.add(new ViewRegistration(timepointId, setupId, transform));
            }
            SequenceDescription sequenceDescription = new SequenceDescription(timepoints, setups, null, missingViews);
            File xmlFile = new File(filePath);
            String imageLoaderPath = xmlFile.getParent() + "/" + imageLoaderElement.getChildText("n5");
            N5FSImageLoader imageLoader = new N5FSImageLoader(new File(imageLoaderPath), sequenceDescription, sharedQueue);
            sequenceDescription.setImgLoader(imageLoader);
            imageLoader.setViewRegistrations(new ViewRegistrations(regs));
            imageLoader.setSeq(sequenceDescription);
            return new SpimData(null, Cast.unchecked(imageLoader.getSequenceDescription()), imageLoader.getViewRegistrations());
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return null;
    }
}
