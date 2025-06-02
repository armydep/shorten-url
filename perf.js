//k6 run --vus 10 --iterations 10 perf.js
//k6 run perf.js
import http from 'k6/http';
import { check, sleep } from 'k6';

function randomAlphaNumeric(length) {
    const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
        result += chars[Math.floor(Math.random() * chars.length)];
    }
    return result;
}

export default function () {
    const randomCode =  randomAlphaNumeric(10);
    const payload = JSON.stringify({ url: 'http://example.k6/' + randomCode });

    // POST request
    const postRes = http.post('http://localhost:8080/api/shorten', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(postRes, {
        'POST status is 200': (r) => r.status === 200,
        'POST has JSON response': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
    });

    const responseBody = postRes.json();
    const extractedCode = responseBody && responseBody.code;

    if (extractedCode) {
        const getRes = http.get(`http://localhost:8080/${responseBody.code}`, {
            redirects: 0, // don't follow redirects so we can check for 301
        });

        check(getRes, {
            'GET status is 301': (r) => r.status === 301,
        });
    }
    else {
            console.warn('POST response did not contain "code" field:', postRes.body);
    }
    sleep(1);
}