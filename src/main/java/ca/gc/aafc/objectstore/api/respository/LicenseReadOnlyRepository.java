package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.YamlPropertyLoaderFactory;
import ca.gc.aafc.objectstore.api.entities.License;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;

@Repository
@PropertySource(value = "classpath:licences.yml", factory = YamlPropertyLoaderFactory.class)
public class LicenseReadOnlyRepository {


    private LinkedList<License> licenses;

    public LinkedList<License> getLicenses(){ return licenses; }

    public void setLicenses(LinkedList<License> licenses) { this.licenses = licenses; }

    public License findOne(String name) {
        for (License l: licenses){
            if (l.getName() == name){
                return l;
            }
        }

    }

}
