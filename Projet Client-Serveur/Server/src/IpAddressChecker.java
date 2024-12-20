
public class IpAddressChecker {
	
	public boolean ipAddressEstValide(String ip) {
		int chiffresIpValide = 0;
		int pointsIpValide = 0;
		int isValidNumber = 0;
		String numberString = "";
		
		if(ip.length() > 6 && ip.length() < 16 ) {
				for(char i :  ip.toCharArray()) {
					if(Character.isDigit(i)) {
						chiffresIpValide += 1;
						numberString += i;
						if (chiffresIpValide > 3) {
							return false;
						}
					} else if (i == '.') {
						pointsIpValide += 1;
						isValidNumber = Integer.parseInt(numberString);
						if (pointsIpValide > 3 | chiffresIpValide == 0) {
							return false;
						} else if(isValidNumber > 255) {
							return false;
						}
						chiffresIpValide = 0;
						isValidNumber = 0;
						numberString = "";
					} else {
						return false;
					}					
				}				
				isValidNumber = Integer.parseInt(numberString);
				if (ip.endsWith(".")) {
					return false;
				} else if (pointsIpValide != 3) {
					return false;
				} else if(isValidNumber > 255) {
					return false;
				}
		} else {
			return false;
		}
		return true;
	}
}
