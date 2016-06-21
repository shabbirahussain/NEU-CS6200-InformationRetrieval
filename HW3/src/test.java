import com.google.common.net.InternetDomainName;

public class test {

	public test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String uriHost = "niesse-tiercel.tumblr.com";
		System.out.println(InternetDomainName.from(uriHost).topPrivateDomain().toString());
	}

}
