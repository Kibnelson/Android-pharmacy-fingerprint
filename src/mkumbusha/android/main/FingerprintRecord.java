/*
 * DbRecord.java
 */

package mkumbusha.android.main;

import java.io.IOException;

import mobihealth.android.util.Base64;

import com.futronictech.SDKHelper.FtrIdentifyRecord;

/**
 * This class represent a user fingerprint database record.
 *
 */
public class FingerprintRecord
{

	//[uuid, name, finger, age, identifier];
	private String name, age, identifier;
	/**
     * User unique key
     */
    private byte[] m_Key;

    /**
     * Finger template.
     */
    private byte[] m_Template;
    
    /**
     * Creates a new instance of DbRecord class.
     * @throws IOException 
     */
    public FingerprintRecord(String uuid, String temp) throws IOException
    {
        // Generate user's unique identifier
        m_Key = uuid.getBytes();
        m_Template = Base64.decode(temp);
    }

    /**
     * Get the user template.
     */
    public byte[] getTemplate()
    {
        return m_Template;
    }

    /**
     * Set the user template.
     */
    public void setTemplate( byte[] value)
    {
        m_Template = value;
    }

    /**
     * Get the user unique identifier.
     */
    public byte[] getUniqueID()
    {
        return m_Key;
    }
    
    public FtrIdentifyRecord getFtrIdentifyRecord()
    {
        FtrIdentifyRecord r = new FtrIdentifyRecord();
        r.m_KeyValue = m_Key;
        r.m_Template = m_Template;
        
        return r;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}
 }
