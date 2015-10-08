package org.hopegames.mobile.task;

public class CourseParticipant {

	String city;
	String country;
	String fullname;
	int id;
	String profileimageurl;
	String profileimageurlsmall;
	UserRoles[] roles;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProfileimageurl() {
		return profileimageurl;
	}

	public void setProfileimageurl(String profileimageurl) {
		this.profileimageurl = profileimageurl;
	}

	public String getProfileimageurlsmall() {
		return profileimageurlsmall;
	}

	public void setProfileimageurlsmall(String profileimageurlsmall) {
		this.profileimageurlsmall = profileimageurlsmall;
	}

	public UserRoles[] getRoles() {
		return roles;
	}

	public void setRoles(UserRoles[] roles) {
		this.roles = roles;
	}

	public class UserRoles {
		String name;
		int roleid;
		String shortname;
		int sortorder;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getRoleid() {
			return roleid;
		}

		public void setRoleid(int roleid) {
			this.roleid = roleid;
		}

		public String getShortname() {
			return shortname;
		}

		public void setShortname(String shortname) {
			this.shortname = shortname;
		}

		public int getSortorder() {
			return sortorder;
		}

		public void setSortorder(int sortorder) {
			this.sortorder = sortorder;
		}

	}
}
