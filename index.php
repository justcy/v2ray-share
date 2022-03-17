<?php
$keys = [
	'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDLv9aXACqsOF3xQ/VE5eipW/RtuP9X0RddBX9D7I7bnVzBHU0AaG5GIJ4b8VkesUr5+LLHzc037FkMhWxy3TSjKAcDIbEIgW7riNsL0xHQ3imr3LjeDv+H/GF+jWVFYmO/ZTwXSOyKGBhwG9llj/ZlHyXqQ33iE9FlTlXio+RxQDF6HaM9lPRWHHAxGnoQv0JBLUjoLAmVeKVEwBE2wa9EGY0obKed+VXoa+5b+3taeAJdXKYxsJWVQOfIdtZYriGR4GUoi2kkyWCc+Ksi308hyClDlEWZPxbC2Yq7wyOdJBYzrxeeB2GGdqjT2YcbGPKtYI5JN3uHeQWQ+Wbc1O2z root@i0cbf75cb8eb7b65151uxef',
	'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC2Bk62SSoqVSADRHmYPPkzo525azC9MatNXAcSVzsArEEgX10fpRJB+HJ53ZXpmAMq3rzYlb/sXLIrG34GSXxKDCIxVjGiJphXzejoClOS7vTzIvNqXjloteQxJgNDUDbKIjiEPV9dIUAK5FGfXBZHS+mbbXondg5PugMnxH596sTQaPvVOEEVPf3w/9U6gLp2RWUS18kdQgIVakGhLWuyrPmvmBHBpHg7C7fzANuwZzgSEWKkXL47NXUBDcSTUkiAkwjkelwHD3dHmMcnsdAA558yt1nwOaX1FSDI6NBmgboaWMI2Wys0xJuTtPFAgIBGtEhzhlINBsCpzpG5zLGP root@i09164bd00662086c5owxa'
];

function getIp(){
    if (getenv("HTTP_CLIENT_IP") && strcasecmp(getenv("HTTP_CLIENT_IP"), "unknown")){
        $ip = getenv("HTTP_CLIENT_IP");
    }else if (getenv("HTTP_X_FORWARDED_FOR") && strcasecmp(getenv("HTTP_X_FORWARDED_FOR"), "unknown")){
        $ip = getenv("HTTP_X_FORWARDED_FOR");
    }else if (getenv("REMOTE_ADDR") && strcasecmp(getenv("REMOTE_ADDR"), "unknown")){
        $ip = getenv("REMOTE_ADDR");
    }else if (isset($_SERVER['REMOTE_ADDR']) && $_SERVER['REMOTE_ADDR'] && strcasecmp($_SERVER['REMOTE_ADDR'], "unknown")){
        $ip = $_SERVER['REMOTE_ADDR'];
    }else{
        $ip = "unknown";
    }
    return($ip);
}
if(getIp()!='unknown'){
	shell_exec("echo $ip > ips;sort -k2n ips|uniq>ips.html;rm -rf ips &");

}
foreach ($keys as $key => $value) {
	echo $value;
}