// Copyright (C) 2011-2018 JovalCM.com.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;
import java.util.Date;

import jsaf.identity.IdentityException;

/**
 * The IUser interface provides information about a Windows user.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUser extends IPrincipal {
    /**
     * Groups this user is a member of.
     *
     * @since 1.4
     */
    Collection<IGroup> memberOf() throws IdentityException;

    /**
     * Get the full name of the user.
     *
     * @since 1.3.5
     */
    String getFullName() throws IdentityException;

    /**
     * Get the comment for the user.
     *
     * @since 1.3.5
     */
    String getComment() throws IdentityException;

    /**
     * Get the Date on which the user's password was last changed. (This will be 00:00:00, Jan 1, 1970 if it was never set).
     *
     * @since 1.3.5
     */
    Date getLastPwdChg() throws IdentityException;

    /**
     * Get the UAC flags.
     *
     * @since 1.3.5
     */
    int getFlags() throws IdentityException;

    /**
     * Test for the presence of a UAC flag.
     *
     * @since 1.3.5
     */
    boolean testFlag(Flag flag) throws IdentityException;

    /**
     * Enumeration of UAC flags.
     * @see <a href="https://msdn.microsoft.com/en-us/library/aa772300(v=vs.85).aspx">ADS_USER_FLAG_ENUM</a>
     */
    enum Flag {
	/**
	 * The logon script is executed.
	 */
	ADS_UF_SCRIPT(0x00000001),

	/**
	 * The user account is disabled.
	 */
	ADS_UF_ACCOUNTDISABLE(0x00000002),

	/**
	 * The home directory is required.
	 */
	ADS_UF_HOMEDIR_REQUIRED(0x00000008),

	/**
	 * The account is currently locked out.
	 */
	ADS_UF_LOCKOUT(0x00000010),

	/**
	 * No password is required.
	 */
	ADS_UF_PASSWD_NOTREQD(0x00000020),

	/**
	 * The user cannot change the password.
	 * Note  You cannot assign the permission settings of PASSWD_CANT_CHANGE by directly modifying the
	 * UserAccountControl attribute.
	 */
	ADS_UF_PASSWD_CANT_CHANGE(0x00000040),

	/**
	 * The user can send an encrypted password.
	 */
	ADS_UF_ENCRYPTED_TEXT_PASSWORD_ALLOWED(0x00000080),

	/**
	 * This is an account for users whose primary account is in another domain. This account provides user
	 * access to this domain, but not to any domain that trusts this domain. Also known as a local user account.
	 */
	ADS_UF_TEMP_DUPLICATE_ACCOUNT(0x00000100),

	/**
	 * This is a default account type that represents a typical user.
	 */
	ADS_UF_NORMAL_ACCOUNT(0x00000200),

	/**
	 * This is a permit to trust account for a system domain that trusts other domains.
	 */
	ADS_UF_INTERDOMAIN_TRUST_ACCOUNT(0x00000800),

	/**
	 * This is a computer account for a computer that is a member of this domain.
	 */
	ADS_UF_WORKSTATION_TRUST_ACCOUNT(0x00001000),

	/**
	 * This is a computer account for a system backup domain controller that is a member of this domain.
	 */
	ADS_UF_SERVER_TRUST_ACCOUNT(0x00002000),

	/**
	 * The password for this account will never expire.
	 */
	ADS_UF_DONT_EXPIRE_PASSWD(0x00010000),

	/**
	 * This is an MNS logon account.
	 */
	ADS_UF_MNS_LOGON_ACCOUNT(0x00020000),

	/**
	 * The user must log on using a smart card.
	 */
	ADS_UF_SMARTCARD_REQUIRED(0x00040000),

	/**
	 * The service account (user or computer account), under which a service runs, is trusted for Kerberos
	 * delegation. Any such service can impersonate a client requesting the service.
	 */
	ADS_UF_TRUSTED_FOR_DELEGATION(0x00080000),

	/**
	 * The security context of the user will not be delegated to a service even if the service account is
	 * set as trusted for Kerberos delegation.
	 */
	ADS_UF_NOT_DELEGATED(0x00100000),

	/**
	 * Restrict this principal to use only Data Encryption Standard (DES) encryption types for keys.
	 */
	ADS_UF_USE_DES_KEY_ONLY(0x00200000),

	/**
	 * This account does not require Kerberos pre-authentication for logon.
	 */
	ADS_UF_DONT_REQUIRE_PREAUTH(0x00400000),

	/**
	 * The user password has expired. This flag is created by the system using data from the Pwd-Last-Set
	 * attribute and the domain policy.
	 */
	ADS_UF_PASSWORD_EXPIRED(0x00800000),

	/**
	 * The account is enabled for delegation. This is a security-sensitive setting; accounts with this option
	 * enabled should be strictly controlled. This setting enables a service running under the account to
	 * assume a client identity and authenticate as that user to other remote servers on the network.
	 */
	ADS_UF_TRUSTED_TO_AUTHENTICATE_FOR_DELEGATION(0x01000000);

	private final int value;

	private Flag(int value) {
	    this.value = value;
	}

	public int value() {
	    return value;
	}
    }
}

